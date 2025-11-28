package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final IncentiveClient incentiveClient;

    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository transactionRecordRepository,
                              IncentiveClient incentiveClient) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.incentiveClient = incentiveClient;
    }

    @Transactional
    public void process(Transaction transaction) {
        if (transaction == null) {
            logger.warn("Received null transaction, skipping");
            return;
        }

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            logger.warn("Invalid transaction participants sender={} recipient={}", transaction.getSenderId(), transaction.getRecipientId());
            return;
        }

        float amount = transaction.getAmount();
        if (sender.getBalance() < amount) {
            logger.warn("Insufficient funds for sender {} balance={} amount={}", sender.getId(), sender.getBalance(), amount);
            return;
        }

        float incentiveAmount = incentiveClient.fetchIncentive(transaction);

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmount);

        transactionRecordRepository.save(new TransactionRecord(sender, recipient, amount, incentiveAmount));
        userRepository.save(sender);
        userRepository.save(recipient);
    }
}

