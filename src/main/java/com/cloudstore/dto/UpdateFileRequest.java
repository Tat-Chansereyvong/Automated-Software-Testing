package com.cloudstore.dto;

public class UpdateFileRequest {

    private String name;
    private Long folderId;

    public UpdateFileRequest() {}

    public UpdateFileRequest(String name, Long folderId) {
        this.name = name;
        this.folderId = folderId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }
}
