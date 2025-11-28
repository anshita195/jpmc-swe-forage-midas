package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscribes to the configured Kafka topic and logs incoming transactions.
 */
@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    private final TransactionService transactionService;
    private final AtomicInteger receivedCount = new AtomicInteger(0);

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}")
    public void onTransaction(Transaction transaction) {
        transactionService.process(transaction);
        int index = receivedCount.incrementAndGet();
        if (index <= 4) {
            logger.info("Transaction {} amount {}", index, transaction.getAmount());
        } else {
            logger.debug("Received transaction {}", transaction);
        }
    }
}

