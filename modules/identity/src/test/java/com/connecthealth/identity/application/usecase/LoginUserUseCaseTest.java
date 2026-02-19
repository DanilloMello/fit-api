package com.connecthealth.identity.application.usecase;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.TokenPair;
import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.exception.InvalidCredentialsException;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import com.connecthealth.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private LoginUserUseCase useCase;

    private User existingUser;

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(userRepository, passwordEncoder, jwtService);
        existingUser = new User(UserId.generate(), "João Silva", new Email("joao@email.com"), "$2a$12$hashed");
    }

    @Test
    void login_valid_credentials_returns_tokens() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashed")).thenReturn(true);
        when(jwtService.generateTokens(any(UserId.class), any(Email.class)))
                .thenReturn(new TokenPair("access-token", "refresh-token", 900L));

        AuthTokensDto result = useCase.execute(new LoginUserCommand("joao@email.com", "password123"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.user().email()).isEqualTo("joao@email.com");
    }

    @Test
    void login_wrong_password_throws_InvalidCredentialsException() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$12$hashed")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("joao@email.com", "wrongpassword")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_unknown_email_throws_InvalidCredentialsException() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("unknown@email.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void unknown_email_and_wrong_password_throw_same_exception_type() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        Class<? extends Throwable> exceptionForUnknownEmail = null;
        try {
            useCase.execute(new LoginUserCommand("noone@email.com", "any"));
        } catch (RuntimeException e) {
            exceptionForUnknownEmail = e.getClass();
        }

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        Class<? extends Throwable> exceptionForWrongPassword = null;
        try {
            useCase.execute(new LoginUserCommand("joao@email.com", "wrong"));
        } catch (RuntimeException e) {
            exceptionForWrongPassword = e.getClass();
        }

        assertThat(exceptionForUnknownEmail).isEqualTo(exceptionForWrongPassword);
    }
}
