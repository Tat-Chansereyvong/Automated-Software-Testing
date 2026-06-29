package com.cloudstore.service;

import com.cloudstore.model.User;
import com.cloudstore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages user registration, profile updates, and account deletion.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Registration ──

    /**
     * Register a new user with the default 50 MB quota.
     * Display name defaults to the email prefix if not provided.
     */
    @Transactional
    public User register(String email, String password) {
        return register(email, password, null);
    }

    @Transactional
    public User register(String email, String password, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = email.split("@")[0];
        }

        User user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(password));
        // quotaBytes and usedBytes use entity defaults (50 MB and 0)
        return userRepository.save(user);
    }

    // ── Lookup ──

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    // ── Profile ──

    @Transactional
    public User updateProfile(User user, String displayName, String newPassword) {
        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(user);
    }

    // ── Account Deletion ──

    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    // ── Quota helpers (delegate to the User entity) ──

    @Transactional
    public void addUsedBytes(User user, long bytes) {
        user.setUsedBytes(user.getUsedBytes() + bytes);
        userRepository.save(user);
    }

    @Transactional
    public void subtractUsedBytes(User user, long bytes) {
        user.setUsedBytes(Math.max(0, user.getUsedBytes() - bytes));
        userRepository.save(user);
    }
}
