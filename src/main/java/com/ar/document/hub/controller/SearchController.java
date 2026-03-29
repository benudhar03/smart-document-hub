package com.ar.document.hub.controller;

import com.ar.document.hub.service.OpenSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private OpenSearchService openSearchService;

    @GetMapping("/documents")
    public ResponseEntity<Page<Map<String, Object>>> searchDocuments(
            @RequestParam String query,
            Pageable pageable) {

        Page<Map<String, Object>> results = openSearchService.searchDocuments(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/advanced")
    public ResponseEntity<Page<Map<String, Object>>> advancedSearch(
            @RequestBody Map<String, Object> searchCriteria,
            Pageable pageable) {

        Page<Map<String, Object>> results = openSearchService.advancedSearch(searchCriteria, pageable);
        return ResponseEntity.ok(results);
    }
}