package com.github.yaliyev.cloudstorageservice.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SqsConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);

    public void processFileEvent(String bucket, String key) {
        log.info("Processing file event successfully for bucket: {}, key: {}", bucket, key);
        // Future business logic: metadata storage in PostgreSQL, image processing, etc.
    }
}
