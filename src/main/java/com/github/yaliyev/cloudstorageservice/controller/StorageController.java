package com.github.yaliyev.cloudstorageservice.controller;


import com.github.yaliyev.cloudstorageservice.service.StorageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/init")
    public String initializeBucket() {
        storageService.createBucket();
        return "Bucket created!";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam String fileName, @RequestBody String content) {
        return storageService.uploadFile(fileName, content);
    }

    @GetMapping("/download")
    public String download(@RequestParam String fileName) {
        return storageService.downloadFile(fileName);
    }
}
