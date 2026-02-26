package com.connecthealth.identity.dto;

public record AuthResponse(UserDto user, TokenData tokens) {}
