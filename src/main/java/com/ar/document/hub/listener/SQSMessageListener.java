package com.ar.document.hub.listener;

import com.ar.document.hub.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class SQSMessageListener {

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Scheduled(fixedDelay = 5000)
    public void consumeMessages() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(20)
                .visibilityTimeout(60)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        for (Message message : messages) {
            try {
                processMessage(message);
                deleteMessage(message);
            } catch (Exception e) {
                log.error("Error processing message: {}", message.messageId(), e);
            }
        }
    }

    private void processMessage(Message message) throws Exception {
        Map<String, Object> messageBody = objectMapper.readValue(message.body(), Map.class);

        UUID documentId = UUID.fromString((String) messageBody.get("documentId"));
        String s3Key = (String) messageBody.get("s3Key");

        log.info("Processing document from SQS: {}", documentId);

        // Process the document (extract text, analyze, etc.)
        documentService.processDocument(documentId, s3Key);
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteRequest);
        log.info("Deleted message from queue: {}", message.messageId());
    }
}