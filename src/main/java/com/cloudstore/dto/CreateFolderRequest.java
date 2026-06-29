package com.cloudstore.dto;

public class CreateFolderRequest {

    private String name;
    private Long parentId;

    public CreateFolderRequest() {}

    public CreateFolderRequest(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
