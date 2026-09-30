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
        storageService.createBucket();
        sqsService.initQueue(); // Creates 'file-events-queue' if it doesn't exist
        return ResponseEntity.ok("S3 Bucket and SQS Queue initialized successfully!");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String fileName, @RequestBody String content) {
        storageService.uploadFile(fileName, content);

        // Publish SQS event
        sqsService.sendFileUploadEvent(fileName);

        return ResponseEntity.ok("File uploaded to S3 and event published to SQS!");
    }

    @GetMapping("/download")
    public String download(@RequestParam String fileName) {
        return storageService.downloadFile(fileName);
    }
}
