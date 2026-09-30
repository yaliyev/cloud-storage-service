package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // Create S3 bucket if it doesn't exist
    public void createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());
    }

    // Upload content to S3
    public String uploadFile(String fileName, String content) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("text/plain")
                .build();

        s3Client.putObject(objectRequest, RequestBody.fromString(content));
        return "File uploaded successfully: " + fileName;
    }

    public String downloadFile(String fileName) {
        software.amazon.awssdk.core.ResponseBytes<software.amazon.awssdk.services.s3.model.GetObjectResponse> objectBytes =
                s3Client.getObjectAsBytes(b -> b.bucket(bucketName).key(fileName));
        return objectBytes.asUtf8String();
    }
}
