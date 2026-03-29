package com.ar.document.hub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SNSService {

    @Autowired
    private SnsClient snsClient;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    public void sendDocumentProcessedNotification(UUID documentId, String fileName, String status) {
        try {
            String message = String.format(
                    "Document %s (ID: %s) has been processed with status: %s",
                    fileName, documentId, status
            );

            Map<String, String> messageAttributes = new HashMap<>();
            messageAttributes.put("documentId", documentId.toString());
            messageAttributes.put("status", status);

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .subject("Document Processing Complete")
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);

            log.info("Notification sent via SNS. MessageId: {}, DocumentId: {}",
                    response.messageId(), documentId);

        } catch (Exception e) {
            log.error("Error sending SNS notification", e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }
}