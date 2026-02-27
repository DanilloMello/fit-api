package com.connecthealth.identity.security;

import com.connecthealth.identity.model.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // Same secret as application.yml
    private static final String SECRET = "Y29ubmVjdGhlYWx0aC1qd3Qtc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQ=";

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800L);

        User user = new User("Alice", "alice@example.com", "hash");
        principal = new UserPrincipal(user);
    }

    @Test
    void generateAccessToken_returnsNonBlankToken() {
        String token = jwtService.generateAccessToken(principal);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateRefreshToken_returnsNonBlankToken() {
        String token = jwtService.generateRefreshToken(principal);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsEmail() {
        String token = jwtService.generateAccessToken(principal);
        assertEquals("alice@example.com", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(principal);
        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateAccessToken(principal);

        User other = new User("Bob", "bob@example.com", "hash");
        UserPrincipal otherPrincipal = new UserPrincipal(other);

        assertFalse(jwtService.isTokenValid(token, otherPrincipal));
    }

    @Test
    void isTokenValid_expiredToken_throwsJwtException() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String expiredToken = jwtService.generateAccessToken(principal);

        assertThrows(JwtException.class, () -> jwtService.isTokenValid(expiredToken, principal));
    }
}
