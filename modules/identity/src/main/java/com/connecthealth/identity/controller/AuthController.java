package com.connecthealth.identity.controller;

import com.connecthealth.identity.dto.AuthResponse;
import com.connecthealth.identity.dto.LoginRequest;
import com.connecthealth.identity.dto.RefreshTokenRequest;
import com.connecthealth.identity.dto.RegisterRequest;
import com.connecthealth.identity.service.AuthService;
import com.connecthealth.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        AuthResponse response = authService.refresh(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
