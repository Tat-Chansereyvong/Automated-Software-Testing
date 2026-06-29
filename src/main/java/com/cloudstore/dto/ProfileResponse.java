package com.cloudstore.dto;

import com.cloudstore.model.User;

/**
 * Profile response returned by GET /api/me.
 * Includes quota usage so the client can display storage info.
 */
public class ProfileResponse {

    private Long id;
    private String email;
    private String displayName;
    private long quotaBytes;
    private long usedBytes;
    private long freeBytes;

    // ── Constructors ──

    public ProfileResponse() {}

    /** Build a ProfileResponse directly from a User entity. */
    public static ProfileResponse from(User user) {
        ProfileResponse r = new ProfileResponse();
        r.id = user.getId();
        r.email = user.getEmail();
        r.displayName = user.getDisplayName();
        r.quotaBytes = user.getQuotaBytes();
        r.usedBytes = user.getUsedBytes();
        r.freeBytes = user.getQuotaBytes() - user.getUsedBytes();
        return r;
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public long getQuotaBytes() { return quotaBytes; }
    public void setQuotaBytes(long quotaBytes) { this.quotaBytes = quotaBytes; }

    public long getUsedBytes() { return usedBytes; }
    public void setUsedBytes(long usedBytes) { this.usedBytes = usedBytes; }

    public long getFreeBytes() { return freeBytes; }
    public void setFreeBytes(long freeBytes) { this.freeBytes = freeBytes; }
}
