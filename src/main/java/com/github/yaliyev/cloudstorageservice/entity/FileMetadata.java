package com.github.yaliyev.cloudstorageservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bucketName", "objectKey"})
})
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String objectKey;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    public enum FileStatus {
        UPLOADED,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public FileMetadata() {}

    public FileMetadata(String bucketName, String objectKey, Long fileSize, FileStatus status) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getBucketName() { return bucketName; }
    public String getObjectKey() { return objectKey; }
    public Long getFileSize() { return fileSize; }
    public FileStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setStatus(FileStatus status) { this.status = status; }
}
