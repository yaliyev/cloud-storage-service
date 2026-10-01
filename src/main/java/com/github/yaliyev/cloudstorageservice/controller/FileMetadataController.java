package com.github.yaliyev.cloudstorageservice.controller;

import com.github.yaliyev.cloudstorageservice.dto.FileMetadataResponse;
import com.github.yaliyev.cloudstorageservice.service.FileMetadataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileMetadataController {

    private final FileMetadataService fileMetadataService;

    public FileMetadataController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @GetMapping
    public ResponseEntity<Page<FileMetadataResponse>> listAllFiles(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(fileMetadataService.getAllFiles(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadataResponse> getFileById(@PathVariable UUID id) {
        // Calling the method here marks it as USED!
        return ResponseEntity.ok(fileMetadataService.getFileWithDownloadUrl(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileMetadataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
