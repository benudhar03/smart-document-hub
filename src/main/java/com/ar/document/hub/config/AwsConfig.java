package com.ar.document.hub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public DefaultCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient(Region region, DefaultCredentialsProvider credentialsProvider) {
        return SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public SqsClient sqsClient(Region region, DefaultCredentialsProvider credentialsProvider) {
        return SqsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public SnsClient snsClient(Region region, DefaultCredentialsProvider credentialsProvider) {
        return SnsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(Region region, DefaultCredentialsProvider credentialsProvider) {
        return DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public CognitoIdentityProviderClient cognitoClient(Region region, DefaultCredentialsProvider credentialsProvider) {
        return CognitoIdentityProviderClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}