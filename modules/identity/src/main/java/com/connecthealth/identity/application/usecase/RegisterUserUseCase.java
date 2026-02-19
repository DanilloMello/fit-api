package com.connecthealth.identity.application.usecase;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.TokenPair;
import com.connecthealth.identity.application.dto.UserDto;
import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.exception.EmailAlreadyExistsException;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import com.connecthealth.identity.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterUserUseCase(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthTokensDto execute(RegisterUserCommand command) {
        Email email = new Email(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = new User(UserId.generate(), command.name(), email, passwordHash);
        User saved = userRepository.save(user);

        TokenPair tokens = jwtService.generateTokens(saved.getId(), saved.getEmail());
        UserDto userDto = new UserDto(saved.getId().toString(), saved.getName(), saved.getEmail().getValue());

        return AuthTokensDto.of(userDto, tokens);
    }
}
