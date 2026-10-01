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

    @SqsListener("file-events-queue")
    public void handleS3Event(S3EventNotification notification) {
        log.info("📩 [MAIN QUEUE] Received event from SQS");
        sqsConsumerService.processS3Event(notification);
    }

    @SqsListener("file-events-queue-dlq")
    public void handleDeadLetterQueue(S3EventNotification notification) {
        log.error("🚨 [DLQ ALERT] Poison pill message detected in DLQ! Needs investigation: {}", notification);
    }
}
