package com.ar.document.hub.controller;

import com.ar.document.hub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/redis-test")
public class RedisTestController {
    
    @Autowired
    private RedisService redisService;
    
    @PostMapping("/set")
    public Map<String, String> setValue(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        
        redisService.setValueWithTTL(key, value, 5, java.util.concurrent.TimeUnit.MINUTES);
        
        return Map.of(
            "status", "success",
            "key", key,
            "value", value
        );
    }
    
    @GetMapping("/get/{key}")
    public Map<String, Object> getValue(@PathVariable String key) {
        Object value = redisService.getValue(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("exists", value != null);
        
        return response;
    }
    
    @GetMapping("/cache-test/{id}")
    @Cacheable(value = "test-cache", key = "#id")
    public Map<String, Object> cacheTest(@PathVariable String id) {
        // Simulate expensive operation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return Map.of(
            "id", id,
            "timestamp", System.currentTimeMillis(),
            "random", UUID.randomUUID().toString()
        );
    }
}