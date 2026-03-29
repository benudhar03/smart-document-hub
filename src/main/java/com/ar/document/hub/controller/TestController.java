package com.ar.document.hub.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "username", jwt.getClaim("cognito:username"),
            "email", jwt.getClaim("email"),
            "groups", jwt.getClaim("cognito:groups"),
            "token", jwt.getTokenValue()
        );
    }
}