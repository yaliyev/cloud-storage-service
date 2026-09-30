package com.github.yaliyev.cloudstorageservice.listener;

import com.github.yaliyev.cloudstorageservice.dto.S3EventNotification;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileEventListener.class);

    @SqsListener("file-events-queue")
    public void handleS3Event(S3EventNotification notification) {
        if (notification.records() == null || notification.records().isEmpty()) {
            return;
        }

        notification.records().forEach(record -> {
            String eventName = record.eventName();
            String bucket = record.s3().bucket().name();
            String key = record.s3().object().key();
            Long size = record.s3().object().size();

            log.info("🔔 [EVENT RECEIVED] Event: {}, Bucket: {}, File: {}, Size: {} bytes",
                    eventName, bucket, key, size);

            // Business logic goes here (e.g., save metadata to database, generate thumbnails)
        });
    }
}
