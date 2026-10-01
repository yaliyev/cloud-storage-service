package com.github.yaliyev.cloudstorageservice.listener;

import com.github.yaliyev.cloudstorageservice.dto.S3EventNotification;
import com.github.yaliyev.cloudstorageservice.service.SqsConsumerService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileEventListener.class);
    private final SqsConsumerService sqsConsumerService;

    public FileEventListener(SqsConsumerService sqsConsumerService) {
        this.sqsConsumerService = sqsConsumerService;
    }

    // 1. Main Queue Listener - Normal successful processing
    @SqsListener("file-events-queue")
    public void handleS3Event(S3EventNotification notification) {
        if (notification.records() != null && !notification.records().isEmpty()) {
            var record = notification.records().get(0);
            String bucket = record.s3().bucket().name();
            String key = record.s3().object().key();

            log.info("📩 [MAIN QUEUE] Received S3 event for Bucket: {}, Key: {}", bucket, key);

            // Delegate business logic (e.g., metadata persistence) to your service layer
            sqsConsumerService.processFileEvent(bucket, key);
        }
    }

    // 2. DLQ Listener - Kept for production alerting if unexpected runtime errors occur
    @SqsListener("file-events-queue-dlq")
    public void handleDeadLetterQueue(S3EventNotification notification) {
        log.error("🚨 [DLQ ALERT] Message landed in DLQ after exhausting retries! Payload: {}", notification);
    }
}
