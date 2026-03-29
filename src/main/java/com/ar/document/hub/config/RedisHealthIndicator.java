package com.ar.document.hub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            // Test Redis connection
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();

            if ("PONG".equals(pingResult)) {
                builder.up()
                        .withDetail("connection", "OK")
                        .withDetail("ping", pingResult);
            } else {
                builder.down()
                        .withDetail("connection", "Failed")
                        .withDetail("ping", pingResult);
            }
        } catch (Exception e) {
            builder.down()
                    .withDetail("error", e.getMessage());
        }
    }
}