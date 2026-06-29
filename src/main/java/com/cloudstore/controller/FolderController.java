package com.cloudstore.controller;

import com.cloudstore.dto.CreateFolderRequest;
import com.cloudstore.dto.FolderResponse;
import com.cloudstore.dto.UpdateFolderRequest;
import com.cloudstore.model.Folder;
import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD operations for folders, scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final StorageService storageService;
    private final UserService userService;

    public FolderController(StorageService storageService, UserService userService) {
        this.storageService = storageService;
        this.userService = userService;
    }

    /** POST /api/folders → create a new folder. */
    @PostMapping
    public ResponseEntity<FolderResponse> create(Principal principal,
                                                  @RequestBody CreateFolderRequest req) {
        User user = userService.findByEmail(principal.getName());
        Folder folder = storageService.createFolder(user, req.getName(), req.getParentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FolderResponse.from(folder));
    }

    /** GET /api/folders?parent=ID → list folders (null parent = root). */
    @GetMapping
    public ResponseEntity<List<FolderResponse>> list(Principal principal,
                                                      @RequestParam(required = false) Long parent) {
        User user = userService.findByEmail(principal.getName());
        List<FolderResponse> folders = storageService.listFolders(user, parent).stream()
                .map(FolderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(folders);
    }

    /** PATCH /api/folders/{id} → rename and/or move a folder. */
    @PatchMapping("/{id}")
    public ResponseEntity<FolderResponse> update(Principal principal,
                                                  @PathVariable Long id,
                                                  @RequestBody UpdateFolderRequest req) {
        User user = userService.findByEmail(principal.getName());
        Folder folder = null;
        if (req.getName() != null) {
            folder = storageService.renameFolder(user, id, req.getName());
        }
        if (req.getParentId() != null) {
            folder = storageService.moveFolder(user, id, req.getParentId());
        }
        if (folder == null) {
            // No changes requested — return current state
            folder = storageService.listFolders(user, null).stream()
                    .filter(f -> f.getId().equals(id))
                    .findFirst()
                    .orElseThrow();
        }
        return ResponseEntity.ok(FolderResponse.from(folder));
    }

    /** DELETE /api/folders/{id} → recursively delete a folder and its contents. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        User user = userService.findByEmail(principal.getName());
        storageService.deleteFolder(user, id);
        return ResponseEntity.noContent().build();
    }
}
