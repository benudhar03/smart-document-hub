package com.ar.document.hub.model;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@DynamoDbBean
public class DocumentMetadata {

    private UUID documentId;
    private String fileName;
    private Map<String, String> extractedMetadata;
    private Integer pageCount;
    private String author;
    private String[] keywords;
    private Long viewCount;
    private Map<String, Object> customFields;

    @DynamoDbPartitionKey
    public UUID getDocumentId() {
        return documentId;
    }
}