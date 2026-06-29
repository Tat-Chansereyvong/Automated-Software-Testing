package com.cloudstore.dto;

import com.cloudstore.model.FileEntity;

import java.time.LocalDateTime;

public class FileResponse {

    private Long id;
    private String name;
    private Long folderId;
    private long sizeBytes;
    private String contentType;
    private LocalDateTime createdAt;

    public FileResponse() {}

    public static FileResponse from(FileEntity file) {
        FileResponse r = new FileResponse();
        r.id = file.getId();
        r.name = file.getName();
        r.folderId = file.getFolder() != null ? file.getFolder().getId() : null;
        r.sizeBytes = file.getSizeBytes();
        r.contentType = file.getContentType();
        r.createdAt = file.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
