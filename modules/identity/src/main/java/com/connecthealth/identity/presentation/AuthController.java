package com.connecthealth.identity.presentation;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.usecase.LoginUserCommand;
import com.connecthealth.identity.application.usecase.LoginUserUseCase;
import com.connecthealth.identity.application.usecase.RefreshTokenUseCase;
import com.connecthealth.identity.application.usecase.RegisterUserCommand;
import com.connecthealth.identity.application.usecase.RegisterUserUseCase;
import com.connecthealth.identity.presentation.request.LoginRequest;
import com.connecthealth.identity.presentation.request.RefreshTokenRequest;
import com.connecthealth.identity.presentation.request.RegisterRequest;
import com.connecthealth.shared.application.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponseData> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokensDto dto = registerUserUseCase.execute(
                new RegisterUserCommand(request.name(), request.email(), request.password())
        );
        return toApiResponse(dto);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponseData> login(@Valid @RequestBody LoginRequest request) {
        AuthTokensDto dto = loginUserUseCase.execute(
                new LoginUserCommand(request.email(), request.password())
        );
        return toApiResponse(dto);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponseData> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokensDto dto = refreshTokenUseCase.execute(request.refreshToken());
        return toApiResponse(dto);
    }

    private ApiResponse<AuthResponseData> toApiResponse(AuthTokensDto dto) {
        AuthResponseData data = new AuthResponseData(
                dto.user(),
                new TokensData(dto.accessToken(), dto.refreshToken(), dto.expiresIn())
        );
        return ApiResponse.success(data, Map.of("timestamp", Instant.now().toString()));
    }

    public record AuthResponseData(
            com.connecthealth.identity.application.dto.UserDto user,
            TokensData tokens
    ) {}

    public record TokensData(String accessToken, String refreshToken, int expiresIn) {}
}
