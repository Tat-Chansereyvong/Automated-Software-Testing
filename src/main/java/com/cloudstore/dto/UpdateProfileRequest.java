package com.cloudstore.dto;

public class UpdateProfileRequest {

    private String displayName;
    private String password;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String displayName, String password) {
        this.displayName = displayName;
        this.password = password;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
