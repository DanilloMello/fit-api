package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.UpdateProfileRequest;
import com.connecthealth.identity.dto.UserResponse;
import com.connecthealth.identity.exception.ResourceNotFoundException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ProfileService profileService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Alice", "alice@example.com", "hash");
    }

    // --- getProfile ---

    @Test
    void getProfile_existingUser_returnsUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = profileService.getProfile(userId);

        assertNotNull(response);
        assertEquals("Alice", response.name());
        assertEquals("alice@example.com", response.email());
    }

    @Test
    void getProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfile(userId));
    }

    // --- updateProfile ---

    @Test
    void updateProfile_allFields_updatesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateProfileRequest req = new UpdateProfileRequest("Bob", "+5511999990000", "https://example.com/photo.jpg");

        UserResponse response = profileService.updateProfile(userId, req);

        assertEquals("Bob", response.name());
        assertEquals("+5511999990000", response.phone());
        assertEquals("https://example.com/photo.jpg", response.photoUrl());
    }

    @Test
    void updateProfile_partialFields_updatesOnlyProvidedFields() {
        user.setPhone("+5511111110000");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateProfileRequest req = new UpdateProfileRequest("Bob", null, null);

        UserResponse response = profileService.updateProfile(userId, req);

        assertEquals("Bob", response.name());
        assertEquals("+5511111110000", response.phone()); // unchanged
        assertNull(response.photoUrl()); // unchanged
    }

    @Test
    void updateProfile_noFields_leavesUserUnchanged() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateProfileRequest req = new UpdateProfileRequest(null, null, null);

        UserResponse response = profileService.updateProfile(userId, req);

        assertEquals("Alice", response.name());
        assertNull(response.phone());
        assertNull(response.photoUrl());
    }

    @Test
    void updateProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        UpdateProfileRequest req = new UpdateProfileRequest("Bob", null, null);

        assertThrows(ResourceNotFoundException.class, () -> profileService.updateProfile(userId, req));
    }
}
