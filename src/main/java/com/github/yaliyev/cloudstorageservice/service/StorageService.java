package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.time.Duration;
import java.util.List;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final SqsClient sqsClient;
    private final String bucketName = "my-test-bucket";
    private final String queueName = "file-events-queue";

    public StorageService(S3Client s3Client, S3Presigner s3Presigner,SqsClient sqsClient) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.sqsClient = sqsClient;
    }

    public void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    public void configureS3BucketNotifications() {
        String queueUrl = sqsClient.getQueueUrl(b -> b.queueName(queueName)).queueUrl();
        String queueArn = sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()).attributes().get(QueueAttributeName.QUEUE_ARN);

        QueueConfiguration queueConfig = QueueConfiguration.builder()
                .queueArn(queueArn)
                .events(Event.S3_OBJECT_CREATED)
                .build();

        NotificationConfiguration notificationConfig = NotificationConfiguration.builder()
                .queueConfigurations(List.of(queueConfig))
                .build();

        s3Client.putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest.builder()
                .bucket(bucketName)
                .notificationConfiguration(notificationConfig)
                .build());
    }


    public String generatePresignedUploadUrl(String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public String generatePresignedDownloadUrl(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteFile(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
    }
}
