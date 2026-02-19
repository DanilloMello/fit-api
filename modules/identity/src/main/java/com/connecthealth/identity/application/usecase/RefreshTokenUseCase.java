package com.connecthealth.identity.application.usecase;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.TokenPair;
import com.connecthealth.identity.application.dto.UserDto;
import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.exception.InvalidCredentialsException;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.UserId;
import com.connecthealth.identity.infrastructure.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stateless refresh token strategy (MVP - Sprint 1).
 * The refresh token is validated and a new token pair is issued.
 * Token rotation (invalidating the old refresh token in DB) is deferred to a future sprint.
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenUseCase {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public RefreshTokenUseCase(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthTokensDto execute(String refreshToken) {
        UserId userId = jwtService.validateRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        TokenPair tokens = jwtService.generateTokens(user.getId(), user.getEmail());
        UserDto userDto = new UserDto(user.getId().toString(), user.getName(), user.getEmail().getValue());

        return AuthTokensDto.of(userDto, tokens);
    }
}
