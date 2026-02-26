package com.connecthealth.identity.security;

public record TokenPair(String accessToken, String refreshToken, long expiresIn) {}
