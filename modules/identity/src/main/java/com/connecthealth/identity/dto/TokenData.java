package com.connecthealth.identity.dto;

public record TokenData(String accessToken, String refreshToken, int expiresIn) {}
