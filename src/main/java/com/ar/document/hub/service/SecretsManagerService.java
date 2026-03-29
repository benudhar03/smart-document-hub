package com.ar.document.hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

@Service
@Slf4j
public class SecretsManagerService {
    
    @Autowired
    private SecretsManagerClient secretsManagerClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${aws.secretsmanager.secret-id}")
    private String secretId;
    
    public Map<String, String> getDatabaseCredentials() {
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretId)
                    .build();
            
            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
            String secretString = valueResponse.secretString();
            
            return objectMapper.readValue(secretString, Map.class);
            
        } catch (Exception e) {
            log.error("Error fetching secrets from Secrets Manager", e);
            throw new RuntimeException("Failed to fetch secrets", e);
        }
    }
    
    public String getSecretValue(String key) {
        Map<String, String> secrets = getDatabaseCredentials();
        return secrets.get(key);
    }
}