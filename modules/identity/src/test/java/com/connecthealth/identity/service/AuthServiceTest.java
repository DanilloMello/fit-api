package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.AuthResponse;
import com.connecthealth.identity.dto.request.LoginRequest;
import com.connecthealth.identity.dto.request.RegisterRequest;
import com.connecthealth.identity.exception.EmailAlreadyExistsException;
import com.connecthealth.identity.exception.InvalidCredentialsException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import com.connecthealth.identity.security.JwtService;
import com.connecthealth.identity.security.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final TokenPair STUB_TOKENS = new TokenPair("access-token", "refresh-token", 900L);

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_new_user_returns_auth_response() {
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "password123");

        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokens(any(UUID.class), anyString())).thenReturn(STUB_TOKENS);

        AuthResponse result = authService.register(request);

        assertThat(result.tokens().accessToken()).isEqualTo("access-token");
        assertThat(result.tokens().refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().name()).isEqualTo("João Silva");
        assertThat(result.user().email()).isEqualTo("joao@email.com");
    }

    @Test
    void register_duplicate_email_throws_EmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("João", "joao@email.com", "password123");

        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("joao@email.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_encodes_password_before_saving() {
        RegisterRequest request = new RegisterRequest("João", "joao@email.com", "password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokens(any(UUID.class), anyString())).thenReturn(STUB_TOKENS);

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$12$hashed");
    }

    @Test
    void login_valid_credentials_returns_auth_response() {
        User existingUser = new User(USER_ID, "João Silva", "joao@email.com", "$2a$12$hashed", null, null, Instant.now());

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashed")).thenReturn(true);
        when(jwtService.generateTokens(any(UUID.class), anyString())).thenReturn(STUB_TOKENS);

        AuthResponse result = authService.login(new LoginRequest("joao@email.com", "password123"));

        assertThat(result.tokens().accessToken()).isEqualTo("access-token");
        assertThat(result.user().email()).isEqualTo("joao@email.com");
    }

    @Test
    void login_wrong_password_throws_InvalidCredentialsException() {
        User existingUser = new User(USER_ID, "João Silva", "joao@email.com", "$2a$12$hashed", null, null, Instant.now());

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$12$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("joao@email.com", "wrongpassword")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_unknown_email_throws_InvalidCredentialsException() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@email.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_and_unknown_email_throw_same_exception_type() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Class<? extends Throwable> exNoUser = null;
        try {
            authService.login(new LoginRequest("noone@email.com", "any"));
        } catch (RuntimeException e) {
            exNoUser = e.getClass();
        }

        User existingUser = new User(USER_ID, "João", "joao@email.com", "$2a$12$hashed", null, null, Instant.now());
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        Class<? extends Throwable> exWrongPw = null;
        try {
            authService.login(new LoginRequest("joao@email.com", "wrong"));
        } catch (RuntimeException e) {
            exWrongPw = e.getClass();
        }

        assertThat(exNoUser).isEqualTo(exWrongPw);
    }
}
