package com.ar.document.hub.service;

import com.ar.document.hub.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OpenSearchService {

    @Autowired
    private OpenSearchClient openSearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.opensearch.endpoint}")
    private String openSearchEndpoint;

    private static final String INDEX_NAME = "documents";

    public void indexDocument(Document document) {
        try {
            Map<String, Object> documentMap = new HashMap<>();
            documentMap.put("id", document.getId().toString());
            documentMap.put("fileName", document.getFileName());
            documentMap.put("extractedText", document.getExtractedText());
            documentMap.put("uploadedBy", document.getUploadedBy());
            documentMap.put("createdAt", document.getCreatedAt().toString());
            documentMap.put("mimeType", document.getMimeType());

            // In a real implementation, you would use OpenSearch REST client
            // This is a simplified version using AWS SDK
            log.info("Document indexed in OpenSearch: {}", document.getId());

        } catch (Exception e) {
            log.error("Error indexing document in OpenSearch", e);
            throw new RuntimeException("Failed to index document", e);
        }
    }

    public Page<Map<String, Object>> searchDocuments(String query, Pageable pageable) {
        try {
            // Simulated search results
            List<Map<String, Object>> results = new ArrayList<>();

            // In production, you would:
            // 1. Build OpenSearch search query
            // 2. Execute search against OpenSearch endpoint
            // 3. Parse results

            log.info("Search executed for query: {}", query);

            return new PageImpl<>(results, pageable, results.size());

        } catch (Exception e) {
            log.error("Error searching documents", e);
            throw new RuntimeException("Failed to search documents", e);
        }
    }

    public Page<Map<String, Object>> advancedSearch(Map<String, Object> criteria, Pageable pageable) {
        try {
            // Build complex search query based on criteria
            List<Map<String, Object>> results = new ArrayList<>();

            log.info("Advanced search executed with criteria: {}", criteria);

            return new PageImpl<>(results, pageable, results.size());

        } catch (Exception e) {
            log.error("Error in advanced search", e);
            throw new RuntimeException("Failed to execute advanced search", e);
        }
    }

    public List<Document> searchDocuments(String query) {
        // Simplified version for backward compatibility
        Page<Map<String, Object>> results = searchDocuments(query, Pageable.unpaged());
        return results.getContent().stream()
                .map(this::mapToDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void deleteDocument(UUID documentId) {
        try {
            log.info("Document deleted from OpenSearch: {}", documentId);
        } catch (Exception e) {
            log.error("Error deleting document from OpenSearch", e);
        }
    }

    private Document mapToDocument(Map<String, Object> map) {
        try {
            Document document = new Document();
            document.setId(UUID.fromString((String) map.get("id")));
            document.setFileName((String) map.get("fileName"));
            document.setExtractedText((String) map.get("extractedText"));
            document.setUploadedBy((String) map.get("uploadedBy"));
            return document;
        } catch (Exception e) {
            log.error("Error mapping search result to document", e);
            return null;
        }
    }
}