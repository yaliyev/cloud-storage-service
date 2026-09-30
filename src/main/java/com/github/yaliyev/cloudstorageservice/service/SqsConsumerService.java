package com.github.yaliyev.cloudstorageservice.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SqsConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);

    @SqsListener("file-events-queue")
    public void listenToFileEvents(String message) {
        log.info("Received event from SQS: {}", message);

        // Simulating downstream processing (e.g., image thumbnailing, metadata indexing)
        log.info("Processing file event successfully!");
    }
}
