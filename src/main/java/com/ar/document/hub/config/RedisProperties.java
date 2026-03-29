package com.ar.document.hub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisProperties {
    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int database = 0;
    private long timeout = 2000;
    private int maxActive = 8;
    private int maxIdle = 8;
    private int minIdle = 0;
    private long maxWait = -1;
}