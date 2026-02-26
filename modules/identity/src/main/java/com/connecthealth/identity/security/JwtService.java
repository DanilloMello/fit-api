package com.connecthealth.identity.security;

import com.connecthealth.identity.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokens(UUID userId, String email) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getAccessTokenExpiry() * 1000))
                .signWith(signingKey)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getRefreshTokenExpiry() * 1000))
                .signWith(signingKey)
                .compact();

        return new TokenPair(accessToken, refreshToken, properties.getAccessTokenExpiry());
    }

    public Claims validateAccessToken(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Access token has expired", e);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid access token", e);
        }
    }

    public UUID validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get(CLAIM_TYPE, String.class);
            if (!TOKEN_TYPE_REFRESH.equals(type)) {
                throw new InvalidTokenException("Token is not a refresh token");
            }
            return UUID.fromString(claims.getSubject());
        } catch (InvalidTokenException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Refresh token has expired", e);
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid refresh token", e);
        }
    }

    public UUID extractUserId(String token) {
        try {
            Claims claims = parseClaims(token);
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return UUID.fromString(e.getClaims().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Cannot extract user ID from token", e);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
