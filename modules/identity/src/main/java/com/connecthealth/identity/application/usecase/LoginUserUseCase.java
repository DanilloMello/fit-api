package com.connecthealth.identity.application.usecase;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.TokenPair;
import com.connecthealth.identity.application.dto.UserDto;
import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.exception.InvalidCredentialsException;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUserUseCase(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthTokensDto execute(LoginUserCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenPair tokens = jwtService.generateTokens(user.getId(), user.getEmail());
        UserDto userDto = new UserDto(user.getId().toString(), user.getName(), user.getEmail().getValue());

        return AuthTokensDto.of(userDto, tokens);
    }
}
