package com.github.yaliyev.cloudstorageservice.service;

import com.github.yaliyev.cloudstorageservice.entity.FileMetadata;
import com.github.yaliyev.cloudstorageservice.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;

import java.time.Duration;

@Service
public class SqsConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private final FileMetadataRepository repository;
    private final IdempotencyService idempotencyService;

    public SqsConsumerService(FileMetadataRepository repository, IdempotencyService idempotencyService) {
        this.repository = repository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public void processS3Event(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            log.warn("Received empty message payload");
            return;
        }

        // Parse using AWS SDK's native JSON parser
        S3EventNotification notification = S3EventNotification.fromJson(messagePayload);

        // Filter out S3 test events (LocalStack initialization events)
        if (notification.getRecords() == null || notification.getRecords().isEmpty()) {
            log.info("Ignoring non-record S3 event (e.g. s3:TestEvent)");
            return;
        }

        for (S3EventNotificationRecord record : notification.getRecords()) {
            String eventName = record.getEventName();
            var s3Entity = record.getS3();

            String bucketName = s3Entity.getBucket().getName();
            String objectKey = s3Entity.getObject().getUrlDecodedKey();
            Long fileSize = s3Entity.getObject().getSizeAsLong();

            // Extract eTag or fallback to sequencer
            String eTag = s3Entity.getObject().getETag();
            if (eTag == null || eTag.isBlank()) {
                eTag = s3Entity.getObject().getSequencer();
            }
            if (eTag == null || eTag.isBlank()) {
                eTag = "default-etag";
            }

            // Construct unique key for Redis
            String idempotencyKey = String.format("event:s3:%s:%s:%s", bucketName, objectKey, eTag);

            // 1. Atomic Redis Check-and-Set
            boolean isFirstTime = idempotencyService.processIfFirstTime(idempotencyKey, Duration.ofHours(24));
            if (!isFirstTime) {
                log.info("Skipping duplicate S3 event [Key: {}] already processed in Redis.", idempotencyKey);
                return;
            }

            log.info("Processing S3 event '{}' for Bucket: {}, Key: {}, Size: {} bytes",
                    eventName, bucketName, objectKey, fileSize);

            // 2. PostgreSQL Upsert
            FileMetadata metadata = repository.findByBucketNameAndObjectKey(bucketName, objectKey)
                    .orElseGet(() -> new FileMetadata(bucketName, objectKey, fileSize, FileMetadata.FileStatus.UPLOADED));

            metadata.setFileSize(fileSize);
            metadata.setStatus(FileMetadata.FileStatus.COMPLETED);

            FileMetadata saved = repository.save(metadata);
            log.info("Successfully persisted FileMetadata record to PostgreSQL with ID: {}", saved.getId());
        }
    }
}
