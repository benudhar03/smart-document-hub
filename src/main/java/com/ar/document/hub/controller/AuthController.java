package com.ar.document.hub.controller;

import com.ar.document.hub.service.CognitoService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private CognitoService cognitoService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            cognitoService.createUser(request.getEmail(), request.getName());
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthenticationResultType authResult = cognitoService.authenticateUser(
                    request.getUsername(), request.getPassword());

            return ResponseEntity.ok(Map.of(
                    "accessToken", authResult.accessToken(),
                    "idToken", authResult.idToken(),
                    "refreshToken", authResult.refreshToken(),
                    "expiresIn", authResult.expiresIn()
            ));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // Invalidate token logic
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String name;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}