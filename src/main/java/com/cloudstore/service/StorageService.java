package com.cloudstore.service;

import com.cloudstore.exception.QuotaExceededException;
import com.cloudstore.exception.ResourceNotFoundException;
import com.cloudstore.model.FileEntity;
import com.cloudstore.model.Folder;
import com.cloudstore.model.User;
import com.cloudstore.repository.FileRepository;
import com.cloudstore.repository.FolderRepository;
import com.cloudstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages files and folders for authenticated users.
 * Every operation is scoped to the requesting user to enforce isolation.
 */
@Service
public class StorageService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Value("${storage.root-dir}")
    private String rootDir;

    public StorageService(FileRepository fileRepository,
                          FolderRepository folderRepository,
                          UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FOLDERS
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Folder createFolder(User owner, String name, Long parentId) {
        Folder parent = resolveParentFolder(owner, parentId);
        Folder folder = new Folder(owner, name, parent);
        return folderRepository.save(folder);
    }

    @Transactional(readOnly = true)
    public List<Folder> listFolders(User owner, Long parentId) {
        if (parentId == null) {
            return folderRepository.findByOwnerAndParentIsNull(owner);
        }
        Folder parent = folderRepository.findByIdAndOwner(parentId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", parentId));
        return folderRepository.findByOwnerAndParent(owner, parent);
    }

    @Transactional
    public Folder renameFolder(User owner, Long folderId, String newName) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));
        folder.setName(newName);
        return folderRepository.save(folder);
    }

    @Transactional
    public Folder moveFolder(User owner, Long folderId, Long newParentId) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));
        Folder newParent = resolveParentFolder(owner, newParentId);
        folder.setParent(newParent);
        return folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(User owner, Long folderId) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));
        deleteFolderRecursive(owner, folder);
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Upload a file into the user's storage.
     * Checks quota BEFORE writing to disk.
     *
     * @throws QuotaExceededException if the upload would exceed the quota
     */
    @Transactional
    public FileEntity uploadFile(User owner, String fileName, byte[] content,
                                 String contentType, Long folderId) {
        long fileSize = content.length;

        // ── Quota check ──
        if (owner.getUsedBytes() + fileSize > owner.getQuotaBytes()) {
            throw new QuotaExceededException(owner.getQuotaBytes(), owner.getUsedBytes(), fileSize);
        }

        // ── Resolve target folder ──
        Folder folder = resolveParentFolder(owner, folderId);

        // ── Write bytes to disk ──
        String storagePath = owner.getId() + "/" + UUID.randomUUID();
        Path diskPath = Paths.get(rootDir, storagePath);
        try {
            Files.createDirectories(diskPath.getParent());
            Files.write(diskPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file to disk", e);
        }

        // ── Persist metadata ──
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        FileEntity file = new FileEntity(owner, folder, fileName, fileSize, contentType, storagePath);
        file = fileRepository.save(file);

        // ── Update used bytes ──
        owner.setUsedBytes(owner.getUsedBytes() + fileSize);
        userRepository.save(owner);

        return file;
    }

    /** Convenience overload without content type. */
    @Transactional
    public FileEntity uploadFile(User owner, String fileName, byte[] content, Long folderId) {
        return uploadFile(owner, fileName, content, "application/octet-stream", folderId);
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(User owner, Long fileId) {
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
        Path diskPath = Paths.get(rootDir, file.getStoragePath());
        try {
            return Files.readAllBytes(diskPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from disk", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileEntity> listFiles(User owner, Long folderId) {
        if (folderId == null) {
            return fileRepository.findByOwnerAndFolderIsNull(owner);
        }
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", folderId));
        return fileRepository.findByOwnerAndFolder(owner, folder);
    }

    /** List ALL file names for a user (useful for isolation tests). */
    @Transactional(readOnly = true)
    public List<String> listAllFileNames(User owner) {
        return fileRepository.findAllByOwner(owner).stream()
                .map(FileEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileEntity renameFile(User owner, Long fileId, String newName) {
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
        file.setName(newName);
        return fileRepository.save(file);
    }

    @Transactional
    public FileEntity moveFile(User owner, Long fileId, Long newFolderId) {
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
        Folder newFolder = resolveParentFolder(owner, newFolderId);
        file.setFolder(newFolder);
        return fileRepository.save(file);
    }

    @Transactional
    public void deleteFile(User owner, Long fileId) {
        FileEntity file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
        deleteFileFromDisk(file);
        owner.setUsedBytes(Math.max(0, owner.getUsedBytes() - file.getSizeBytes()));
        userRepository.save(owner);
        fileRepository.delete(file);
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUOTA HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** Free space = quota − used. */
    public long freeBytes(User owner) {
        return owner.getQuotaBytes() - owner.getUsedBytes();
    }

    /** Total bytes used. */
    public long usedBytes(User owner) {
        return owner.getUsedBytes();
    }

    // ═══════════════════════════════════════════════════════════════
    //  ACCOUNT CLEANUP (called when a user deletes their account)
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public void deleteAllUserData(User owner) {
        // Delete file bytes from disk
        List<FileEntity> files = fileRepository.findAllByOwner(owner);
        for (FileEntity file : files) {
            deleteFileFromDisk(file);
        }
        // Delete DB records
        fileRepository.deleteAllByOwner(owner);
        folderRepository.deleteAllByOwner(owner);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Folder resolveParentFolder(User owner, Long parentId) {
        if (parentId == null) return null;
        return folderRepository.findByIdAndOwner(parentId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", parentId));
    }

    private void deleteFolderRecursive(User owner, Folder folder) {
        // Delete child folders first
        List<Folder> children = folderRepository.findByOwnerAndParent(owner, folder);
        for (Folder child : children) {
            deleteFolderRecursive(owner, child);
        }
        // Delete files in this folder
        List<FileEntity> files = fileRepository.findByOwnerAndFolder(owner, folder);
        for (FileEntity file : files) {
            deleteFileFromDisk(file);
            owner.setUsedBytes(Math.max(0, owner.getUsedBytes() - file.getSizeBytes()));
            fileRepository.delete(file);
        }
        userRepository.save(owner);
        folderRepository.delete(folder);
    }

    private void deleteFileFromDisk(FileEntity file) {
        try {
            Path diskPath = Paths.get(rootDir, file.getStoragePath());
            Files.deleteIfExists(diskPath);
        } catch (IOException e) {
            // Log but don't fail — DB is the source of truth
        }
    }
}
