package com.github.yaliyev.cloudstorageservice.controller;


import com.github.yaliyev.cloudstorageservice.service.SqsService;
import com.github.yaliyev.cloudstorageservice.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    private final SqsService sqsService;

    public StorageController(StorageService storageService, SqsService sqsService) {
        this.storageService = storageService;
        this.sqsService = sqsService;
    }

    @PostMapping("/init")
    public ResponseEntity<String> initializeServices() {
        storageService.createBucketIfNotExists();
        sqsService.initQueue(); // Creates 'file-events-queue' if it doesn't exist
        return ResponseEntity.ok("S3 Bucket and SQS Queue initialized successfully!");
    }


    @GetMapping("/presigned-download")
    public ResponseEntity<String> getPresignedDownloadUrl(@RequestParam String fileName) {
        String url = storageService.generatePresignedDownloadUrl(fileName);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/presigned-upload")
    public ResponseEntity<String> getPresignedUploadUrl(@RequestParam String fileName) {
        String url = storageService.generatePresignedUploadUrl(fileName);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        storageService.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully from S3: " + fileName);
    }
}
