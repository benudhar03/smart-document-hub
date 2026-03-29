package com.ar.document.hub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CognitoService {

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    public AdminCreateUserResponse createUser(String email, String name) {
        Map<String, String> userAttributes = new HashMap<>();
        userAttributes.put("email", email);
        userAttributes.put("name", name);
        userAttributes.put("email_verified", "true");

        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .userAttributes(userAttributes.entrySet().stream()
                        .map(entry -> AttributeType.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build())
                        .toList())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .build();

        AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);
        log.info("User created in Cognito: {}", email);

        return response;
    }

    public AuthenticationResultType authenticateUser(String username, String password) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();

        InitiateAuthResponse response = cognitoClient.initiateAuth(authRequest);
        log.info("User authenticated: {}", username);

        return response.authenticationResult();
    }

    public AdminGetUserResponse getUser(String username) {
        AdminGetUserRequest request = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        return cognitoClient.adminGetUser(request);
    }
}