package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsService {

    private final SqsClient sqsClient;
    private final String queueName = "file-events-queue";

    public SqsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void initQueue() {
        sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .build());
    }

    public void sendFileUploadEvent(String fileName) {
        String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();

        String messageBody = String.format("{\"event\": \"FILE_UPLOADED\", \"fileName\": \"%s\"}", fileName);

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build());
    }
}
