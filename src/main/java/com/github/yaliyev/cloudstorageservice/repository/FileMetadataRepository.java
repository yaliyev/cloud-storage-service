package com.github.yaliyev.cloudstorageservice.repository;


import com.github.yaliyev.cloudstorageservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    Optional<FileMetadata> findByBucketNameAndObjectKey(String bucketName, String objectKey);
}
