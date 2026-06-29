package com.cloudstore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A virtual folder inside a user's storage space.
 * Supports nested folders via the parent reference.
 */
@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this folder. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    /** Nullable — null means this is a root-level folder. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──

    public Folder() {}

    public Folder(User owner, String name, Folder parent) {
        this.owner = owner;
        this.name = name;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Folder getParent() { return parent; }
    public void setParent(Folder parent) { this.parent = parent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
