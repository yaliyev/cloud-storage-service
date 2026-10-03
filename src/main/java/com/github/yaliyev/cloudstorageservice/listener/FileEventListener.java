package com.github.yaliyev.cloudstorageservice.listener;

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
    public void handleS3Event(String messagePayload) {
        log.info("📩 [MAIN QUEUE] Received raw SQS payload");
        sqsConsumerService.processS3Event(messagePayload);
    }

    @SqsListener("file-events-queue-dlq")
    public void handleDeadLetterQueue(String messagePayload) {
        log.error("🚨 [DLQ ALERT] Poison pill or unparseable event in DLQ: {}", messagePayload);
    }
}
