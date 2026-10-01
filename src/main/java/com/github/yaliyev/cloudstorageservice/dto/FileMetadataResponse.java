package com.github.yaliyev.cloudstorageservice.dto;

import com.github.yaliyev.cloudstorageservice.entity.FileMetadata;

import java.time.Instant;
import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        String bucketName,
        String objectKey,
        Long fileSize,
        FileMetadata.FileStatus status,
        Instant createdAt,
        Instant updatedAt,
        String downloadUrl
) {
    public static FileMetadataResponse fromEntity(FileMetadata entity, String downloadUrl) {
        return new FileMetadataResponse(
                entity.getId(),
                entity.getBucketName(),
                entity.getObjectKey(),
                entity.getFileSize(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                downloadUrl
        );
    }

    public static FileMetadataResponse fromEntity(FileMetadata entity) {
        return fromEntity(entity, null);
    }
}
