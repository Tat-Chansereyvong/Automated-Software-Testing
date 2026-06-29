package com.cloudstore.controller;

import com.cloudstore.dto.ProfileResponse;
import com.cloudstore.dto.RegisterRequest;
import com.cloudstore.model.User;
import com.cloudstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Handles user registration and login.
 * Both endpoints are accessible without authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** POST /api/auth/register → create a new user with 50 MB quota. */
    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.getEmail(), req.getPassword(), req.getDisplayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfileResponse.from(user));
    }

    /**
     * POST /api/auth/login → validate credentials (via HTTP Basic header) and return profile.
     * Spring Security authenticates the request before it reaches this method.
     */
    @PostMapping("/login")
    public ResponseEntity<ProfileResponse> login(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(ProfileResponse.from(user));
    }
}
