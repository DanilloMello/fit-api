package com.connecthealth.identity.application.dto;

public record TokenPair(String accessToken, String refreshToken, long expiresIn) {
}
