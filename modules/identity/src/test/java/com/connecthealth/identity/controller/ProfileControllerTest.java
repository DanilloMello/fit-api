package com.connecthealth.identity.controller;

import com.connecthealth.identity.dto.UpdateProfileRequest;
import com.connecthealth.identity.dto.UserResponse;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.security.JwtService;
import com.connecthealth.identity.security.SecurityConfig;
import com.connecthealth.identity.security.UserDetailsServiceImpl;
import com.connecthealth.identity.security.UserPrincipal;
import com.connecthealth.identity.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProfileService profileService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private UserPrincipal principal;
    private UUID userId;
    private UserResponse stubUserResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User("Alice", "alice@example.com", "hash");
        principal = new UserPrincipal(user);

        stubUserResponse = new UserResponse(
                userId, "Alice", "alice@example.com",
                "+5511999990000", null, LocalDateTime.now()
        );
    }

    // --- GET /api/v1/profile ---

    @Test
    void getProfile_authenticated_returns200() throws Exception {
        when(profileService.getProfile(any())).thenReturn(stubUserResponse);

        mockMvc.perform(get("/api/v1/profile").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }

    @Test
    void getProfile_unauthenticated_returns4xx() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().is4xxClientError());
    }

    // --- PATCH /api/v1/profile ---

    @Test
    void updateProfile_authenticated_returns200() throws Exception {
        UserResponse updated = new UserResponse(
                userId, "Bob", "alice@example.com",
                "+5511000000000", "https://example.com/photo.jpg", LocalDateTime.now()
        );
        when(profileService.updateProfile(any(), any(UpdateProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/profile")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bob","phone":"+5511000000000","photoUrl":"https://example.com/photo.jpg"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bob"))
                .andExpect(jsonPath("$.data.phone").value("+5511000000000"));
    }

    @Test
    void updateProfile_unauthenticated_returns4xx() throws Exception {
        mockMvc.perform(patch("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bob"}
                                """))
                .andExpect(status().is4xxClientError());
    }
}
