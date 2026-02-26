package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.AuthResponse;
import com.connecthealth.identity.dto.TokenData;
import com.connecthealth.identity.dto.UserDto;
import com.connecthealth.identity.dto.request.LoginRequest;
import com.connecthealth.identity.dto.request.RegisterRequest;
import com.connecthealth.identity.exception.EmailAlreadyExistsException;
import com.connecthealth.identity.exception.InvalidCredentialsException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import com.connecthealth.identity.security.JwtService;
import com.connecthealth.identity.security.TokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User(UUID.randomUUID(), request.name(), email,
                passwordEncoder.encode(request.password()), null, null, Instant.now());
        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        UUID userId = jwtService.validateRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        TokenPair tokens = jwtService.generateTokens(user.getId(), user.getEmail());
        UserDto userDto = new UserDto(user.getId().toString(), user.getName(), user.getEmail());
        TokenData tokenData = new TokenData(tokens.accessToken(), tokens.refreshToken(), (int) tokens.expiresIn());
        return new AuthResponse(userDto, tokenData);
    }
}
