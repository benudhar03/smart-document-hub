package com.ar.document.hub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Key prefixes
    private static final String DOCUMENT_CACHE_KEY = "document:";
    private static final String SESSION_CACHE_KEY = "session:";
    private static final String USER_CACHE_KEY = "user:";
    private static final String RATE_LIMIT_KEY = "rate:";
    private static final String COUNTER_KEY = "counter:";
    private static final String LOCK_KEY = "lock:";
    private static final String QUEUE_KEY = "queue:";

    // Value Operations
    public void setValue(String key, Object value) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value);
            log.debug("Value set in Redis for key: {}", key);
        } catch (Exception e) {
            log.error("Error setting value in Redis", e);
        }
    }

    public void setValueWithTTL(String key, Object value, long timeout, TimeUnit unit) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value, timeout, unit);
            log.debug("Value with TTL set in Redis for key: {}, TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting value with TTL in Redis", e);
        }
    }

    public Object getValue(String key) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.get(key);
        } catch (Exception e) {
            log.error("Error getting value from Redis", e);
            return null;
        }
    }

    // Document caching methods
    public void cacheDocumentContent(UUID documentId, String extractedText) {
        String key = DOCUMENT_CACHE_KEY + documentId;
        setValueWithTTL(key, extractedText, 1, TimeUnit.HOURS);
        log.info("Document content cached for: {}", documentId);
    }

    public String getCachedDocument(UUID documentId) {
        String key = DOCUMENT_CACHE_KEY + documentId;
        Object value = getValue(key);
        return value != null ? value.toString() : null;
    }

    // Session caching methods
    public void cacheCollaborationSession(String sessionId, Object sessionData) {
        String key = SESSION_CACHE_KEY + sessionId;
        setValueWithTTL(key, sessionData, 30, TimeUnit.MINUTES);
        log.info("Collaboration session cached: {}", sessionId);
    }

    public Object getCollaborationSession(String sessionId) {
        String key = SESSION_CACHE_KEY + sessionId;
        return getValue(key);
    }

    // User caching methods
    public void cacheUser(String userId, Object userData) {
        String key = USER_CACHE_KEY + userId;
        setValueWithTTL(key, userData, 2, TimeUnit.HOURS);
        log.info("User data cached for: {}", userId);
    }

    public Object getCachedUser(String userId) {
        String key = USER_CACHE_KEY + userId;
        return getValue(key);
    }

    // Rate limiting methods
    public boolean isRateLimited(String userId, String action, int maxRequests, long timeWindowSeconds) {
        String key = RATE_LIMIT_KEY + userId + ":" + action;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, timeWindowSeconds, TimeUnit.SECONDS);
        }

        return count > maxRequests;
    }

    // Counter methods
    public long incrementCounter(String counterName) {
        String key = COUNTER_KEY + counterName;
        Long count = redisTemplate.opsForValue().increment(key);
        return count != null ? count : 0;
    }

    public long getCounter(String counterName) {
        String key = COUNTER_KEY + counterName;
        Object value = getValue(key);
        return value != null ? Long.parseLong(value.toString()) : 0;
    }

    // Hash Operations
    public void setHashField(String key, String field, Object value) {
        try {
            HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
            ops.put(key, field, value);
            log.debug("Hash field set in Redis for key: {}, field: {}", key, field);
        } catch (Exception e) {
            log.error("Error setting hash field in Redis", e);
        }
    }

    public Object getHashField(String key, String field) {
        try {
            HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
            return ops.get(key, field);
        } catch (Exception e) {
            log.error("Error getting hash field from Redis", e);
            return null;
        }
    }

    public Map<String, Object> getAllHashFields(String key) {
        try {
            HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
            return ops.entries(key);
        } catch (Exception e) {
            log.error("Error getting all hash fields from Redis", e);
            return Collections.emptyMap();
        }
    }

    // List Operations
    public void pushToList(String key, Object value) {
        try {
            ListOperations<String, Object> ops = redisTemplate.opsForList();
            ops.leftPush(key, value);
            log.debug("Value pushed to list in Redis for key: {}", key);
        } catch (Exception e) {
            log.error("Error pushing to list in Redis", e);
        }
    }

    public Object popFromList(String key) {
        try {
            ListOperations<String, Object> ops = redisTemplate.opsForList();
            return ops.rightPop(key);
        } catch (Exception e) {
            log.error("Error popping from list in Redis", e);
            return null;
        }
    }

    public List<Object> getListRange(String key, long start, long end) {
        try {
            ListOperations<String, Object> ops = redisTemplate.opsForList();
            return ops.range(key, start, end);
        } catch (Exception e) {
            log.error("Error getting list range from Redis", e);
            return Collections.emptyList();
        }
    }

    // Set Operations
    public void addToSet(String key, Object... values) {
        try {
            SetOperations<String, Object> ops = redisTemplate.opsForSet();
            ops.add(key, values);
            log.debug("Values added to set in Redis for key: {}", key);
        } catch (Exception e) {
            log.error("Error adding to set in Redis", e);
        }
    }

    public Set<Object> getSetMembers(String key) {
        try {
            SetOperations<String, Object> ops = redisTemplate.opsForSet();
            return ops.members(key);
        } catch (Exception e) {
            log.error("Error getting set members from Redis", e);
            return Collections.emptySet();
        }
    }

    public boolean isSetMember(String key, Object value) {
        try {
            SetOperations<String, Object> ops = redisTemplate.opsForSet();
            Boolean isMember = ops.isMember(key, value);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Error checking set membership in Redis", e);
            return false;
        }
    }

    // Sorted Set Operations
    public void addToSortedSet(String key, Object value, double score) {
        try {
            ZSetOperations<String, Object> ops = redisTemplate.opsForZSet();
            ops.add(key, value, score);
            log.debug("Value added to sorted set in Redis for key: {}, score: {}", key, score);
        } catch (Exception e) {
            log.error("Error adding to sorted set in Redis", e);
        }
    }

    public Set<Object> getSortedSetRange(String key, long start, long end) {
        try {
            ZSetOperations<String, Object> ops = redisTemplate.opsForZSet();
            return ops.range(key, start, end);
        } catch (Exception e) {
            log.error("Error getting sorted set range from Redis", e);
            return Collections.emptySet();
        }
    }

    // Distributed Lock methods
    public boolean acquireLock(String lockName, String lockValue, long timeoutSeconds) {
        String key = LOCK_KEY + lockName;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, lockValue, timeoutSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }

    public boolean releaseLock(String lockName, String lockValue) {
        String key = LOCK_KEY + lockName;
        String currentValue = (String) getValue(key);

        if (lockValue.equals(currentValue)) {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        }
        return false;
    }

    // Queue operations
    public void pushToQueue(String queueName, Object item) {
        String key = QUEUE_KEY + queueName;
        pushToList(key, item);
    }

    public Object popFromQueue(String queueName) {
        String key = QUEUE_KEY + queueName;
        return popFromList(key);
    }

    // Cache eviction methods
    public void removeCache(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cache removed for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error removing cache for key: {}", key, e);
        }
    }

    public void removeByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("Removed {} caches with pattern: {}", deleted, pattern);
            }
        } catch (Exception e) {
            log.error("Error removing caches with pattern: {}", pattern, e);
        }
    }

    // Cache exists check
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking existence of key: {}", key, e);
            return false;
        }
    }

    // Set expiration
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean expired = redisTemplate.expire(key, timeout, unit);
            return Boolean.TRUE.equals(expired);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }

    // Get remaining TTL
    public long getTTL(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return -1;
        }
    }
}