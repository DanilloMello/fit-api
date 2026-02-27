package com.connecthealth.identity.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
