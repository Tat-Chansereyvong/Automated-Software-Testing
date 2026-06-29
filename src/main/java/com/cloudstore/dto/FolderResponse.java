package com.cloudstore.dto;

import com.cloudstore.model.Folder;

import java.time.LocalDateTime;

public class FolderResponse {

    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;

    public FolderResponse() {}

    public static FolderResponse from(Folder folder) {
        FolderResponse r = new FolderResponse();
        r.id = folder.getId();
        r.name = folder.getName();
        r.parentId = folder.getParent() != null ? folder.getParent().getId() : null;
        r.createdAt = folder.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
