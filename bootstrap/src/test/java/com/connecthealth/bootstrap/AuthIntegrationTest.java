package com.connecthealth.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // --- Health ---

    @Test
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // --- Full auth flow ---

    @Test
    void registerAndLoginFlow_returnsTokens() throws Exception {
        // Register
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Integration User","email":"integration@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.email").value("integration@example.com"))
                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty())
                .andReturn();

        String registerBody = registerResult.getResponse().getContentAsString();
        JsonNode registerJson = objectMapper.readTree(registerBody);
        String accessToken = registerJson.at("/data/tokens/accessToken").asText();
        String refreshToken = registerJson.at("/data/tokens/refreshToken").asText();

        // Login
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"integration@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty());

        // Refresh
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty())
                .andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .at("/data/tokens/accessToken").asText();

        // Get profile with original access token
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("integration@example.com"))
                .andExpect(jsonPath("$.data.name").value("Integration User"));

        // Update profile with new access token
        mockMvc.perform(patch("/api/v1/profile")
                        .header("Authorization", "Bearer " + newAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated User","phone":"+5511999990000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated User"))
                .andExpect(jsonPath("$.data.phone").value("+5511999990000"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
                {"name":"Dup User","email":"dup@example.com","password":"password123"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Wrong Pass","email":"wrongpass@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrongpass@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void profile_withoutToken_returns4xx() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void profile_withInvalidToken_returns4xx() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer this.is.not.valid"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"not-a-real-token"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
