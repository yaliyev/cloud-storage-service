package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

@Service
public class DlqAdminService {

    private final SqsClient sqsClient;
    private final String mainQueueUrl;
    private final String dlqUrl;

    public DlqAdminService(
            SqsClient sqsClient,
            @Value("${aws.sqs.queue-url}") String mainQueueUrl,
            @Value("${aws.sqs.dlq-url}") String dlqUrl) {
        this.sqsClient = sqsClient;
        this.mainQueueUrl = mainQueueUrl;
        this.dlqUrl = dlqUrl;
    }

    // Inspect messages currently in the DLQ without removing them
    public List<String> peekDlqMessages(int maxMessages) {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(dlqUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .visibilityTimeout(5) // Short timeout for peeking
                .build();

        return sqsClient.receiveMessage(receiveRequest).messages().stream()
                .map(Message::body)
                .toList();
    }

    // Redrive (move) all messages from DLQ back to Main Queue
    public int redriveMessages(int maxMessages) {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(dlqUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        int movedCount = 0;

        for (Message message : messages) {
            // 1. Send to Main Queue
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(mainQueueUrl)
                    .messageBody(message.body())
                    .build());

            // 2. Delete from DLQ
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(dlqUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

            movedCount++;
        }

        return movedCount;
    }
}
