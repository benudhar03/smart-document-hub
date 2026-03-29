package com.ar.document.hub.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TextExtractionService {

    public String extractText(byte[] fileContent, String mimeType) throws IOException {
        if (mimeType == null) {
            return "";
        }
        return switch (mimeType) {
            case "application/pdf" -> extractTextFromPDF(fileContent);
            case "text/plain" -> new String(fileContent);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    extractTextFromDocx(fileContent);
            default -> {
                log.warn("Unsupported mime type: {}", mimeType);
                yield "";
            }
        };
    }

    private String extractTextFromPDF(byte[] content) throws IOException {
        log.info("Extracting text from PDF, content size: {} bytes", content.length);

        // Try multiple approaches to load PDF
        PDDocument document = null;
        try {
            // Approach 1: Using Loader class (PDFBox 3.x)
            document = Loader.loadPDF(content);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                log.warn("No text extracted from PDF");
                return "";
            }

            log.info("Successfully extracted {} characters from PDF", text.length());
            return text;

        } catch (Exception e) {
            log.error("Error extracting text from PDF using Loader", e);
                return "";
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    log.warn("Error closing PDF document", e);
                }
            }
        }
    }


    private String extractTextFromDocx(byte[] content) {
        // In production, use Apache POI or similar library
        // Simplified version for demo
        return "DOCX content extraction would happen here";
    }

    public int getPageCount(byte[] fileContent, String mimeType) {
        if ("application/pdf".equals(mimeType)) {
            try (InputStream inputStream = new ByteArrayInputStream(fileContent);
                 PDDocument document = Loader.loadPDF(fileContent)) {  // Still use Loader
                return document.getNumberOfPages();
            } catch (Exception e) {
                log.error("Error getting page count", e);
            }
        }
        return 1;
    }

    public String[] extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        // Simple keyword extraction - in production use NLP libraries
        Pattern pattern = Pattern.compile("\\W+");
        String[] words = pattern.split(text.toLowerCase());

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            if (word.length() > 3) { // Ignore short words
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
}