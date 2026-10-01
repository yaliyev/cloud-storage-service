package com.github.yaliyev.cloudstorageservice.service;

import com.github.yaliyev.cloudstorageservice.dto.S3EventNotification;
import com.github.yaliyev.cloudstorageservice.entity.FileMetadata;
import com.github.yaliyev.cloudstorageservice.repository.FileMetadataRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SqsConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private final FileMetadataRepository repository;

    public SqsConsumerService(FileMetadataRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void processS3Event(S3EventNotification notification) {
        if (notification == null || notification.records() == null || notification.records().isEmpty()) {
            log.warn("Received empty or invalid S3 event notification");
            return;
        }

        for (var record : notification.records()) {
            String eventName = record.eventName();
            var s3Entity = record.s3();

            String bucketName = s3Entity.bucket().name();
            String objectKey = s3Entity.object().key();
            Long fileSize = s3Entity.object().size();

            log.info("Processing S3 event '{}' for Bucket: {}, Key: {}, Size: {} bytes",
                    eventName, bucketName, objectKey, fileSize);

            // Upsert Logic: Search existing record or create new
            FileMetadata metadata = repository.findByBucketNameAndObjectKey(bucketName, objectKey)
                    .orElseGet(() -> new FileMetadata(bucketName, objectKey, fileSize, FileMetadata.FileStatus.UPLOADED));

            // Update attributes
            metadata.setFileSize(fileSize);
            metadata.setStatus(FileMetadata.FileStatus.COMPLETED);

            FileMetadata saved = repository.save(metadata);
            log.info("Successfully persisted FileMetadata record to PostgreSQL with ID: {}", saved.getId());
        }
    }
}
