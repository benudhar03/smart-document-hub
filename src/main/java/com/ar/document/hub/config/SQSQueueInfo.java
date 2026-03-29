package com.ar.document.hub.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SQSQueueInfo {
    private String queueUrl;
    private String queueArn;
    private String approximateNumberOfMessages;
    private String visibilityTimeout;
    private String createdTimestamp;
    private String lastModifiedTimestamp;
}