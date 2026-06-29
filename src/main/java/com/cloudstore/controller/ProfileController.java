package com.cloudstore.controller;

import com.cloudstore.dto.ProfileResponse;
import com.cloudstore.dto.UpdateProfileRequest;
import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Manages the authenticated user's profile.
 * GET /api/me   → view profile + quota
 * PUT /api/me   → update display name / password
 * DELETE /api/me → delete own account + all data
 */
@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final UserService userService;
    private final StorageService storageService;

    public ProfileController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Principal principal,
                                                          @RequestBody UpdateProfileRequest req) {
        User user = userService.findByEmail(principal.getName());
        user = userService.updateProfile(user, req.getDisplayName(), req.getPassword());
        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        // Delete all files & folders from storage first
        storageService.deleteAllUserData(user);
        // Then delete the user account
        userService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }
}
