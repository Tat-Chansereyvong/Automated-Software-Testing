package com.cloudstore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a registered user with a personal storage quota.
 * Each user gets 50 MB (52 428 800 bytes) of storage upon registration.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String passwordHash;

    /** Total storage quota in bytes (default 50 MB). */
    @Column(nullable = false)
    private long quotaBytes = 52_428_800L;

    /** Bytes currently used by uploaded files. */
    @Column(nullable = false)
    private long usedBytes = 0L;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──

    public User() {}

    public User(String email, String displayName, String passwordHash) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public long getQuotaBytes() { return quotaBytes; }
    public void setQuotaBytes(long quotaBytes) { this.quotaBytes = quotaBytes; }

    public long getUsedBytes() { return usedBytes; }
    public void setUsedBytes(long usedBytes) { this.usedBytes = usedBytes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
