package com.cloudstore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Metadata for a file stored in a user's cloud storage.
 * Actual bytes are stored on disk; this entity tracks name, size, and location.
 */
@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this file. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** The folder this file lives in (null = root). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private String contentType = "application/octet-stream";

    /** On-disk path relative to storage root. */
    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──

    public FileEntity() {}

    public FileEntity(User owner, Folder folder, String name, long sizeBytes,
                      String contentType, String storagePath) {
        this.owner = owner;
        this.folder = folder;
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
