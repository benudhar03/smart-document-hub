package com.ar.document.hub.service;


import com.ar.document.hub.model.Document;
import com.ar.document.hub.model.DocumentMetadata;
import com.ar.document.hub.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private SQSService sqsService;

    @Autowired
    private SNSService snsService;

    @Autowired
    private DynamoDBService dynamoDBService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private TextExtractionService textExtractionService;

    @Transactional
    public Document uploadDocument(MultipartFile file, String userId) {
        try {
            // Upload to S3
            String s3Key = s3Service.uploadFile(file, userId);

            // Save document metadata to RDS
            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setS3Key(s3Key);
            document.setFileSize(file.getSize());
            document.setMimeType(file.getContentType());
            document.setUploadedBy(userId);
            document.setStatus(Document.DocumentStatus.PENDING);
            document.setCreatedAt(LocalDateTime.now());

            Document savedDocument = documentRepository.save(document);

            // Save additional metadata to DynamoDB
            DocumentMetadata metadata = DocumentMetadata.builder()
                    .documentId(savedDocument.getId())
                    .fileName(file.getOriginalFilename())
                    .viewCount(0L)
                    .build();
            dynamoDBService.saveMetadata(metadata);

            // Send message to SQS for async processing
            sqsService.sendDocumentProcessingMessage(savedDocument.getId(), s3Key);

            log.info("Document uploaded successfully: {}", savedDocument.getId());
            return savedDocument;

        } catch (IOException e) {
            log.error("Error uploading document", e);
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Transactional
    public void processDocument(UUID documentId, String s3Key) {
        try {
            log.info("Starting document processing for ID: {}", documentId);

            // Update status to PROCESSING
            Document document = getDocument(documentId);
            document.setStatus(Document.DocumentStatus.PROCESSING);
            documentRepository.save(document);

            // Download from S3 and extract text
            byte[] fileContent = s3Service.downloadFile(s3Key);
            String extractedText = textExtractionService.extractText(fileContent, document.getMimeType());

            // Update document with extracted text
            document.setExtractedText(extractedText);
            document.setStatus(Document.DocumentStatus.COMPLETED);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);

            // Cache in Redis
            redisService.cacheDocumentContent(documentId, extractedText);

            // Index in OpenSearch
            openSearchService.indexDocument(document);

            // Update DynamoDB metadata
            DocumentMetadata metadata = dynamoDBService.getMetadata(documentId);
            if (metadata != null) {
                metadata.setPageCount(textExtractionService.getPageCount(fileContent, document.getMimeType()));
                metadata.setKeywords(textExtractionService.extractKeywords(extractedText));
                dynamoDBService.saveMetadata(metadata);
            }

            // Send SNS notification
            snsService.sendDocumentProcessedNotification(documentId, document.getFileName(), "COMPLETED");

            log.info("Document processing completed for ID: {}", documentId);

        } catch (Exception e) {
            log.error("Error processing document: {}", documentId, e);

            // Update status to FAILED
            Document document = getDocument(documentId);
            document.setStatus(Document.DocumentStatus.FAILED);
            documentRepository.save(document);

            // Send failure notification
            snsService.sendDocumentProcessedNotification(documentId, document.getFileName(), "FAILED");

            throw new RuntimeException("Failed to process document", e);
        }
    }

    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
    }

    public List<Document> getUserDocuments(String userId) {
        return documentRepository.findByUploadedByOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = getDocument(documentId);

        // Delete from S3
        s3Service.deleteFile(document.getS3Key());

        // Delete from RDS
        documentRepository.delete(document);

        // Delete from DynamoDB
        // Note: DynamoDB delete would be implemented

        // Delete from OpenSearch
        openSearchService.deleteDocument(documentId);

        // Remove from Redis cache
        redisService.removeCache("document:" + documentId);

        log.info("Document deleted: {}", documentId);
    }

    @Transactional(readOnly = true)
    public String getDocumentContent(UUID documentId) {
        // Try Redis cache first
        String cachedContent = redisService.getCachedDocument(documentId);
        if (cachedContent != null) {
            log.info("Document content retrieved from cache: {}", documentId);
            return cachedContent;
        }

        // If not in cache, get from database
        Document document = getDocument(documentId);

        // Update view count in DynamoDB
        dynamoDBService.updateViewCount(documentId);

        return document.getExtractedText();
    }

    public List<Document> searchDocuments(String query) {
        return openSearchService.searchDocuments(query);
    }
}