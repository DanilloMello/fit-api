package com.connecthealth.identity.dto;

public record UpdateProfileRequest(
        String name,
        String phone,
        String photoUrl
) {}
