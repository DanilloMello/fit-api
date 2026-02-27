package com.connecthealth.identity.controller;

import com.connecthealth.identity.dto.UpdateProfileRequest;
import com.connecthealth.identity.dto.UserResponse;
import com.connecthealth.identity.security.UserPrincipal;
import com.connecthealth.identity.service.ProfileService;
import com.connecthealth.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserPrincipal user) {
        UserResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping
    ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        UserResponse response = profileService.updateProfile(user.getId(), req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
