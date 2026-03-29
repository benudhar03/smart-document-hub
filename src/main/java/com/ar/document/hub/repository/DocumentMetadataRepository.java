package com.smartdoc.repository;

import com.ar.document.hub.model.DocumentMetadata;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class DocumentMetadataRepository {

    private final DynamoDbTable<DocumentMetadata> metadataTable;

    public DocumentMetadataRepository(DynamoDbClient dynamoDbClient, String tableName) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.metadataTable = enhancedClient.table(tableName,
                TableSchema.fromBean(DocumentMetadata.class));
    }

    public void save(DocumentMetadata metadata) {
        metadataTable.putItem(metadata);
    }

    public DocumentMetadata findById(UUID documentId) {
        return metadataTable.getItem(r -> r.key(k -> k.partitionValue(documentId.toString())));
    }

    public void deleteById(UUID documentId) {
        metadataTable.deleteItem(r -> r.key(k -> k.partitionValue(documentId.toString())));
    }

    public List<DocumentMetadata> findByAuthor(String author) {
        // In DynamoDB, you would typically use a GSI for this
        // This is a simplified version
        return metadataTable.scan().items().stream()
                .filter(metadata -> author.equals(metadata.getAuthor()))
                .collect(Collectors.toList());
    }

    public void updateViewCount(UUID documentId) {
        DocumentMetadata metadata = findById(documentId);
        if (metadata != null) {
            metadata.setViewCount(metadata.getViewCount() + 1);
            save(metadata);
        }
    }
}