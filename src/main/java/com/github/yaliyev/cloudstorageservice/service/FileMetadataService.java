package com.github.yaliyev.cloudstorageservice.service;

import com.github.yaliyev.cloudstorageservice.dto.FileMetadataResponse;
import com.github.yaliyev.cloudstorageservice.entity.FileMetadata;
import com.github.yaliyev.cloudstorageservice.exception.ResourceNotFoundException;
import com.github.yaliyev.cloudstorageservice.repository.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FileMetadataService {

    private final FileMetadataRepository repository;
    private final StorageService storageService;

    public FileMetadataService(FileMetadataRepository repository, StorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    // List all files WITH download URLs
    public Page<FileMetadataResponse> getAllFiles(Pageable pageable){
        return repository.findAll(pageable)
                .map(entity -> {
                    String downloadUrl = storageService.generatePresignedDownloadUrl(entity.getObjectKey());
                    return FileMetadataResponse.fromEntity(entity, downloadUrl);
                });
    }

//    public FileMetadataResponse getFileById(UUID id) {
//        return repository.findById(id)
//                .map(FileMetadataResponse::fromEntity)
//                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found for ID: " + id));
//    }

    public FileMetadataResponse getFileWithDownloadUrl(UUID id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found for ID: " + id));

        String downloadUrl = storageService.generatePresignedDownloadUrl(metadata.getObjectKey());

        return FileMetadataResponse.fromEntity(metadata, downloadUrl);
    }

    @Transactional
    public void deleteFile(UUID id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found for ID: " + id));

        // 1. Delete physical object from S3 bucket
        storageService.deleteFile(metadata.getObjectKey());

        // 2. Remove record from PostgreSQL
        repository.delete(metadata);
    }

}
