package com.ar.document.hub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;

import java.net.URI;

@Configuration
public class SQSConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.sqs.queue-url:}")
    private String queueUrl;

    @Value("${aws.sqs.endpoint:}")
    private String endpoint;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .endpointOverride(endpoint != null && !endpoint.isEmpty() ? URI.create(endpoint) : null)
                .build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .endpointOverride(endpoint != null && !endpoint.isEmpty() ? URI.create(endpoint) : null)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SQSQueueInfo sqsQueueInfo(SqsClient sqsClient) {
        if (queueUrl != null && !queueUrl.isEmpty()) {
            try {
                GetQueueAttributesRequest attributesRequest = GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(
                                QueueAttributeName.QUEUE_ARN,
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                                QueueAttributeName.VISIBILITY_TIMEOUT
                        )
                        .build();

                GetQueueAttributesResponse attributesResponse = sqsClient.getQueueAttributes(attributesRequest);

                return SQSQueueInfo.builder()
                        .queueUrl(queueUrl)
                        .queueArn(attributesResponse.attributes().get(QueueAttributeName.QUEUE_ARN))
                        .approximateNumberOfMessages(
                                attributesResponse.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                        )
                        .visibilityTimeout(
                                attributesResponse.attributes().get(QueueAttributeName.VISIBILITY_TIMEOUT)
                        )
                        .build();
            } catch (Exception e) {
                // Queue info not available, return basic info
                return SQSQueueInfo.builder()
                        .queueUrl(queueUrl)
                        .build();
            }
        }
        return null;
    }
}