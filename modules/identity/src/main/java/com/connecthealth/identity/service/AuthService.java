package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.AuthResponse;
import com.connecthealth.identity.dto.LoginRequest;
import com.connecthealth.identity.dto.RefreshTokenRequest;
import com.connecthealth.identity.dto.RegisterRequest;
import com.connecthealth.identity.dto.TokenResponse;
import com.connecthealth.identity.dto.UserResponse;
import com.connecthealth.identity.exception.ConflictException;
import com.connecthealth.identity.exception.UnauthorizedException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import com.connecthealth.identity.security.JwtService;
import com.connecthealth.identity.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final long ACCESS_TOKEN_EXPIRES_IN = 900L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already in use");
        }

        User user = new User(req.name(), req.email(), passwordEncoder.encode(req.password()));
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        User user = userRepository.findByRefreshToken(req.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        UserPrincipal principal = new UserPrincipal(user);
        if (!jwtService.isTokenValid(req.refreshToken(), principal)) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        user.setRefreshToken(refreshToken);

        return new AuthResponse(
                UserResponse.from(user),
                new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_EXPIRES_IN)
        );
    }
}
