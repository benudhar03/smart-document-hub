package com.ar.document.hub.service;

import com.ar.document.hub.model.DocumentMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.UUID;

@Service
@Slf4j
public class DynamoDBService {

    private final DynamoDbTable<DocumentMetadata> metadataTable;

    @Autowired
    public DynamoDBService(DynamoDbClient dynamoDbClient,
                           @Value("${aws.dynamodb.table-name}") String tableName) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.metadataTable = enhancedClient.table(tableName,
                TableSchema.fromBean(DocumentMetadata.class));
    }

    public void saveMetadata(DocumentMetadata metadata) {
        metadataTable.putItem(metadata);
        log.info("Document metadata saved to DynamoDB: {}", metadata.getDocumentId());
    }

    public DocumentMetadata getMetadata(UUID documentId) {
        return metadataTable.getItem(r -> r.key(k -> k.partitionValue(documentId.toString())));
    }

    public void updateViewCount(UUID documentId) {
        DocumentMetadata metadata = getMetadata(documentId);
        if (metadata != null) {
            metadata.setViewCount(metadata.getViewCount() + 1);
            metadataTable.updateItem(metadata);
        }
    }
}