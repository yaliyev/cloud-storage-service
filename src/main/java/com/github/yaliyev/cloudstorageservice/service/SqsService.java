package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Map;

@Service
public class SqsService {

    private final SqsClient sqsClient;
    private final String mainQueueName;
    private final String dlqName;

    public SqsService(SqsClient sqsClient, @Value("${aws.sqs.queue-name}") String mainQueueName,
                      @Value("${aws.sqs.dlq-name}") String dlqName) {
        this.sqsClient = sqsClient;
        this.mainQueueName = mainQueueName;
        this.dlqName = dlqName;
    }

    public void initQueue() {
        // 1. Get or Create DLQ
        String dlqUrl = getOrCreateQueue(dlqName);
        String dlqArn = getQueueArn(dlqUrl);

        // 2. Get or Create Main Queue
        String mainQueueUrl = getOrCreateQueue(mainQueueName);
        String mainQueueArn = getQueueArn(mainQueueUrl);

        // 3. Define Policies
        String redrivePolicy = String.format(
                "{\"maxReceiveCount\":\"3\", \"deadLetterTargetArn\":\"%s\"}", dlqArn);

        // Grant S3 permissions to publish to SQS
        String accessPolicy = String.format("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": "sqs:SendMessage",
                      "Resource": "%s"
                    }
                  ]
                }
                """, mainQueueArn);

        // 4. Update Main Queue Attributes
        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(mainQueueUrl)
                .attributes(Map.of(
                        QueueAttributeName.REDRIVE_POLICY, redrivePolicy,
                        QueueAttributeName.POLICY, accessPolicy,
                        QueueAttributeName.VISIBILITY_TIMEOUT, "5" // 5 seconds for fast retry testing
                ))
                .build());
    }

    private String getOrCreateQueue(String queueName) {
        try {
            return sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
        } catch (QueueDoesNotExistException e) {
            return sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build()).queueUrl();
        }
    }

    private String getQueueArn(String queueUrl) {
        return sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()).attributes().get(QueueAttributeName.QUEUE_ARN);
    }
}
