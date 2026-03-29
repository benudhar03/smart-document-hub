package com.ar.document.hub.config;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

@Configuration
public class RedisCacheConfig {
    
    // Cache names
    public static final String DOCUMENTS_CACHE = "documents";
    public static final String SESSIONS_CACHE = "sessions";
    public static final String USERS_CACHE = "users";
    public static final String SEARCH_CACHE = "search";
    
    // Custom cache annotations
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Cacheable(value = DOCUMENTS_CACHE, key = "#documentId")
    public @interface CacheDocument {
    }
    
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @CacheEvict(value = DOCUMENTS_CACHE, key = "#documentId")
    public @interface EvictDocumentCache {
    }
    
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Caching(
        evict = {
            @CacheEvict(value = DOCUMENTS_CACHE, allEntries = true),
            @CacheEvict(value = SEARCH_CACHE, allEntries = true)
        }
    )
    public @interface EvictAllDocumentCaches {
    }
}