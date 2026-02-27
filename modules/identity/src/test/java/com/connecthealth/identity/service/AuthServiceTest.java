package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.AuthResponse;
import com.connecthealth.identity.dto.LoginRequest;
import com.connecthealth.identity.dto.RefreshTokenRequest;
import com.connecthealth.identity.dto.RegisterRequest;
import com.connecthealth.identity.exception.ConflictException;
import com.connecthealth.identity.exception.UnauthorizedException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import com.connecthealth.identity.security.JwtService;
import com.connecthealth.identity.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", "hashed-password");
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
    }

    // --- register ---

    @Test
    void register_newEmail_savesUserAndReturnsTokens() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest req = new RegisterRequest("Alice", "alice@example.com", "password123");
        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("Alice", response.user().name());
        assertEquals("access-token", response.tokens().accessToken());
        assertEquals("refresh-token", response.tokens().refreshToken());
        assertEquals(900L, response.tokens().expiresIn());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
    }

    @Test
    void register_existingEmail_throwsConflictException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest("Alice", "alice@example.com", "password123");

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    // --- login ---

    @Test
    void login_validCredentials_returnsTokens() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest("alice@example.com", "password123");
        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("alice@example.com", response.user().email());
        assertEquals("access-token", response.tokens().accessToken());
    }

    @Test
    void login_badCredentials_throwsUnauthorizedException() {
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        LoginRequest req = new LoginRequest("alice@example.com", "wrong-password");

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void login_userNotFoundAfterAuth_throwsUnauthorizedException() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest("alice@example.com", "password123");

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    // --- refresh ---

    @Test
    void refresh_validToken_returnsNewTokens() {
        user.setRefreshToken("old-refresh-token");
        when(userRepository.findByRefreshToken("old-refresh-token")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq("old-refresh-token"), any(UserPrincipal.class))).thenReturn(true);

        RefreshTokenRequest req = new RefreshTokenRequest("old-refresh-token");
        AuthResponse response = authService.refresh(req);

        assertNotNull(response);
        assertEquals("refresh-token", response.tokens().refreshToken());
        assertEquals("refresh-token", user.getRefreshToken());
    }

    @Test
    void refresh_tokenNotFound_throwsUnauthorizedException() {
        when(userRepository.findByRefreshToken("unknown-token")).thenReturn(Optional.empty());

        RefreshTokenRequest req = new RefreshTokenRequest("unknown-token");

        assertThrows(UnauthorizedException.class, () -> authService.refresh(req));
    }

    @Test
    void refresh_expiredToken_throwsUnauthorizedException() {
        user.setRefreshToken("expired-token");
        when(userRepository.findByRefreshToken("expired-token")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq("expired-token"), any(UserPrincipal.class))).thenReturn(false);

        RefreshTokenRequest req = new RefreshTokenRequest("expired-token");

        assertThrows(UnauthorizedException.class, () -> authService.refresh(req));
    }
}
