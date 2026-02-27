package com.connecthealth.identity.dto;

public record AuthResponse(
        UserResponse user,
        TokenResponse tokens
) {}
