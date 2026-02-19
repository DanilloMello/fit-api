package com.connecthealth.identity.application.dto;

public record AuthTokensDto(UserDto user, String accessToken, String refreshToken, int expiresIn) {

    public static AuthTokensDto of(UserDto user, TokenPair tokens) {
        return new AuthTokensDto(user, tokens.accessToken(), tokens.refreshToken(), (int) tokens.expiresIn());
    }
}
