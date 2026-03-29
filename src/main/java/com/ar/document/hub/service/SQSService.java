package com.ar.document.hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SQSService {

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.dlq-url:}")
    private String deadLetterQueueUrl;

    @Value("${aws.sqs.message-group-id:document-processing}")
    private String messageGroupId;

    public String sendDocumentProcessingMessage(UUID documentId, String s3Key) {
        return sendMessage(documentId, s3Key, "PROCESS", 0);
    }

    public String sendDocumentReprocessingMessage(UUID documentId, String s3Key) {
        return sendMessage(documentId, s3Key, "REPROCESS", 1);
    }

    public String sendDocumentDeletionMessage(UUID documentId, String s3Key) {
        return sendMessage(documentId, s3Key, "DELETE", 0);
    }

    public String sendMessage(UUID documentId, String s3Key, String action, int priority) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("documentId", documentId.toString());
            message.put("s3Key", s3Key);
            message.put("action", action);
            message.put("priority", priority);
            message.put("timestamp", System.currentTimeMillis());
            message.put("correlationId", UUID.randomUUID().toString());

            String messageBody = objectMapper.writeValueAsString(message);

            // Build message request with attributes
            SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageAttributes(createMessageAttributes(documentId, action, priority));

            // Add FIFO queue specific attributes if queue is FIFO
            if (queueUrl.contains(".fifo")) {
                requestBuilder
                        .messageGroupId(messageGroupId)
                        .messageDeduplicationId(UUID.randomUUID().toString());
            }

            // Add delay if needed for priority handling
            if (priority > 0) {
                requestBuilder.delaySeconds(priority * 5); // Higher priority = less delay
            }

            SendMessageRequest sendMessageRequest = requestBuilder.build();
            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            log.info("Message sent to SQS. MessageId: {}, DocumentId: {}, Action: {}",
                    response.messageId(), documentId, action);

            return response.messageId();

        } catch (Exception e) {
            log.error("Error sending message to SQS", e);
            throw new RuntimeException("Failed to send message to SQS", e);
        }
    }

    private Map<String, MessageAttributeValue> createMessageAttributes(UUID documentId, String action, int priority) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();

        attributes.put("documentId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(documentId.toString())
                .build());

        attributes.put("action", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(action)
                .build());

        attributes.put("priority", MessageAttributeValue.builder()
                .dataType("Number")
                .stringValue(String.valueOf(priority))
                .build());

        attributes.put("timestamp", MessageAttributeValue.builder()
                .dataType("Number")
                .stringValue(String.valueOf(System.currentTimeMillis()))
                .build());

        return attributes;
    }

    public void sendMessageWithDelay(UUID documentId, String s3Key, int delaySeconds) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("documentId", documentId.toString());
            message.put("s3Key", s3Key);
            message.put("timestamp", System.currentTimeMillis());

            String messageBody = objectMapper.writeValueAsString(message);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .delaySeconds(delaySeconds)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            log.info("Delayed message sent to SQS. MessageId: {}, DocumentId: {}, Delay: {}s",
                    response.messageId(), documentId, delaySeconds);

        } catch (Exception e) {
            log.error("Error sending delayed message to SQS", e);
            throw new RuntimeException("Failed to send delayed message to SQS", e);
        }
    }

    public void sendBatchMessages(List<Map<String, Object>> messages) {
        try {
            List<SendMessageBatchRequestEntry> entries = new java.util.ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                Map<String, Object> message = messages.get(i);
                String messageBody = objectMapper.writeValueAsString(message);

                SendMessageBatchRequestEntry entry = SendMessageBatchRequestEntry.builder()
                        .id(String.valueOf(i))
                        .messageBody(messageBody)
                        .build();

                entries.add(entry);
            }

            SendMessageBatchRequest batchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(queueUrl)
                    .entries(entries)
                    .build();

            SendMessageBatchResponse response = sqsClient.sendMessageBatch(batchRequest);

            log.info("Batch messages sent. Successful: {}, Failed: {}",
                    response.successful().size(), response.failed().size());

        } catch (Exception e) {
            log.error("Error sending batch messages to SQS", e);
            throw new RuntimeException("Failed to send batch messages", e);
        }
    }

    public int getQueueDepth() {
        try {
            GetQueueAttributesRequest attributesRequest = GetQueueAttributesRequest.builder()
                    .queueUrl(queueUrl)
                    .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                    .build();

            GetQueueAttributesResponse response = sqsClient.getQueueAttributes(attributesRequest);

            String depthStr = response.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
            return depthStr != null ? Integer.parseInt(depthStr) : 0;

        } catch (Exception e) {
            log.error("Error getting queue depth", e);
            return -1;
        }
    }

    public void purgeQueue() {
        try {
            PurgeQueueRequest purgeRequest = PurgeQueueRequest.builder()
                    .queueUrl(queueUrl)
                    .build();

            sqsClient.purgeQueue(purgeRequest);
            log.info("Queue purged: {}", queueUrl);

        } catch (Exception e) {
            log.error("Error purging queue", e);
            throw new RuntimeException("Failed to purge queue", e);
        }
    }
}