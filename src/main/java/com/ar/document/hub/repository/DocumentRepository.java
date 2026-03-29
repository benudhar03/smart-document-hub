package com.ar.document.hub.repository;

import com.ar.document.hub.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);

    List<Document> findByStatus(Document.DocumentStatus status);

    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Document> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.uploadedBy = :userId")
    long countByUploadedBy(@Param("userId") String userId);

    @Query("SELECT d FROM Document d WHERE d.fileName LIKE %:keyword% OR d.extractedText LIKE %:keyword%")
    List<Document> searchByKeyword(@Param("keyword") String keyword);
}