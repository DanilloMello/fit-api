package com.connecthealth.identity.presentation;

import com.connecthealth.identity.application.dto.AuthTokensDto;
import com.connecthealth.identity.application.dto.UserDto;
import com.connecthealth.identity.application.usecase.LoginUserUseCase;
import com.connecthealth.identity.application.usecase.RefreshTokenUseCase;
import com.connecthealth.identity.application.usecase.RegisterUserUseCase;
import com.connecthealth.identity.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    private static final AuthTokensDto STUB_TOKENS = new AuthTokensDto(
            new UserDto("some-uuid", "João Silva", "joao@email.com"),
            "access-token", "refresh-token", 900
    );

    @Test
    void register_with_valid_body_returns_201() throws Exception {
        when(registerUserUseCase.execute(any())).thenReturn(STUB_TOKENS);

        mockMvc.perform(post("/api/v1/auth/register")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Silva",
                                  "email": "joao@email.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.email").value("joao@email.com"))
                .andExpect(jsonPath("$.data.tokens.accessToken").value("access-token"));
    }

    @Test
    void register_with_blank_name_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "joao@email.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_with_valid_body_returns_200() throws Exception {
        when(loginUserUseCase.execute(any())).thenReturn(STUB_TOKENS);

        mockMvc.perform(post("/api/v1/auth/login")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "joao@email.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokens.refreshToken").value("refresh-token"));
    }

    @Test
    void login_with_invalid_credentials_returns_401() throws Exception {
        when(loginUserUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "joao@email.com",
                                  "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
