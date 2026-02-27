package com.connecthealth.identity.service;

import com.connecthealth.identity.dto.UpdateProfileRequest;
import com.connecthealth.identity.dto.UserResponse;
import com.connecthealth.identity.exception.ResourceNotFoundException;
import com.connecthealth.identity.model.User;
import com.connecthealth.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.name() != null) user.setName(req.name());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.photoUrl() != null) user.setPhotoUrl(req.photoUrl());

        return UserResponse.from(user);
    }
}
