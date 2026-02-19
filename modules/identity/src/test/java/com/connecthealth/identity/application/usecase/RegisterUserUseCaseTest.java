package com.connecthealth.identity.application.usecase;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.TokenPair;
import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.exception.EmailAlreadyExistsException;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import com.connecthealth.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_new_user_returns_tokens() {
        RegisterUserCommand command = new RegisterUserCommand("João Silva", "joao@email.com", "password123");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokens(any(UserId.class), any(Email.class)))
                .thenReturn(new TokenPair("access-token", "refresh-token", 900L));

        AuthTokensDto result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().name()).isEqualTo("João Silva");
        assertThat(result.user().email()).isEqualTo("joao@email.com");
    }

    @Test
    void register_duplicate_email_throws_EmailAlreadyExistsException() {
        RegisterUserCommand command = new RegisterUserCommand("João", "joao@email.com", "password123");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("joao@email.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void password_encoder_encode_is_called() {
        RegisterUserCommand command = new RegisterUserCommand("João", "joao@email.com", "password123");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokens(any(UserId.class), any(Email.class)))
                .thenReturn(new TokenPair("access", "refresh", 900L));

        useCase.execute(command);

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void user_repository_save_is_called_with_hashed_password() {
        RegisterUserCommand command = new RegisterUserCommand("João", "joao@email.com", "password123");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokens(any(UserId.class), any(Email.class)))
                .thenReturn(new TokenPair("access", "refresh", 900L));

        useCase.execute(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$12$hashed");
    }
}
