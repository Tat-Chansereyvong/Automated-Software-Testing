package com.cloudstore.controller;

import com.cloudstore.dto.FileResponse;
import com.cloudstore.dto.UpdateFileRequest;
import com.cloudstore.model.FileEntity;
import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Upload, download, rename, move, and delete files — all scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final StorageService storageService;
    private final UserService userService;

    public FileController(StorageService storageService, UserService userService) {
        this.storageService = storageService;
        this.userService = userService;
    }

    /** POST /api/files (multipart) → upload a file. */
    @PostMapping
    public ResponseEntity<FileResponse> upload(Principal principal,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(required = false) Long folderId) {
        User user = userService.findByEmail(principal.getName());
        try {
            FileEntity entity = storageService.uploadFile(
                    user,
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType(),
                    folderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(FileResponse.from(entity));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    /** GET /api/files?folder=ID → list files in a folder (null = root). */
    @GetMapping
    public ResponseEntity<List<FileResponse>> list(Principal principal,
                                                    @RequestParam(required = false) Long folder) {
        User user = userService.findByEmail(principal.getName());
        List<FileResponse> files = storageService.listFiles(user, folder).stream()
                .map(FileResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }

    /** GET /api/files/{id}/download → download file bytes. */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(Principal principal, @PathVariable Long id) {
        User user = userService.findByEmail(principal.getName());
        // Get file metadata for content type and name
        FileEntity fileMeta = storageService.listFiles(user, null).stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);

        byte[] content = storageService.downloadFile(user, id);

        HttpHeaders headers = new HttpHeaders();
        if (fileMeta != null) {
            headers.setContentType(MediaType.parseMediaType(fileMeta.getContentType()));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(fileMeta.getName())
                    .build());
        }
        return ResponseEntity.ok().headers(headers).body(content);
    }

    /** PATCH /api/files/{id} → rename and/or move a file. */
    @PatchMapping("/{id}")
    public ResponseEntity<FileResponse> update(Principal principal,
                                                @PathVariable Long id,
                                                @RequestBody UpdateFileRequest req) {
        User user = userService.findByEmail(principal.getName());
        FileEntity file = null;
        if (req.getName() != null) {
            file = storageService.renameFile(user, id, req.getName());
        }
        if (req.getFolderId() != null) {
            file = storageService.moveFile(user, id, req.getFolderId());
        }
        return ResponseEntity.ok(FileResponse.from(file));
    }

    /** DELETE /api/files/{id} → delete a file and free quota. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        User user = userService.findByEmail(principal.getName());
        storageService.deleteFile(user, id);
        return ResponseEntity.noContent().build();
    }
}
