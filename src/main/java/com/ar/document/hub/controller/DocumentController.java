package com.ar.document.hub.controller;


import com.ar.document.hub.model.Document;
import com.ar.document.hub.service.DocumentService;
import com.ar.document.hub.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId) {

        log.info("Upload request received for file: {} by user: {}", file.getOriginalFilename(), userId);

        Document document = documentService.uploadDocument(file, userId);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable UUID documentId) {
        Document document = documentService.getDocument(documentId);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{documentId}/download-url")
    public ResponseEntity<String> getDownloadUrl(@PathVariable UUID documentId) {
        Document document = documentService.getDocument(documentId);
        String presignedUrl = s3Service.generatePresignedUrl(document.getS3Key(), Duration.ofHours(1));
        return ResponseEntity.ok(presignedUrl);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable String userId) {
        List<Document> documents = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}