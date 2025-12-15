package com.shubham.internship_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        log.info("User requested /me endpoint. Principal: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("claims", jwt.getClaims());

        return response;
    }
}
