package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {

    private static final Logger logger = LoggerFactory.getLogger(IncentiveClient.class);

    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public IncentiveClient(RestTemplateBuilder builder,
                           @Value("${services.incentive.url:http://localhost:8080/incentive}") String incentiveUrl) {
        this.restTemplate = builder.build();
        this.incentiveUrl = incentiveUrl;
    }

    public float fetchIncentive(Transaction transaction) {
        try {
            ResponseEntity<Incentive> response = restTemplate.postForEntity(incentiveUrl, transaction, Incentive.class);
            Incentive incentive = response.getBody();
            return incentive != null ? Math.max(0f, incentive.getAmount()) : 0f;
        } catch (RestClientException ex) {
            logger.warn("Failed to fetch incentive information", ex);
            return 0f;
        }
    }
}

