package com.cloudstore.repository;

import com.cloudstore.model.FileEntity;
import com.cloudstore.model.Folder;
import com.cloudstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    /** List files owned by user in a specific folder. */
    List<FileEntity> findByOwnerAndFolder(User owner, Folder folder);

    /** List files owned by user at root level (folder is null). */
    List<FileEntity> findByOwnerAndFolderIsNull(User owner);

    /** Find a file by id only if it belongs to the given owner (isolation). */
    Optional<FileEntity> findByIdAndOwner(Long id, User owner);

    /** Sum of all file sizes for a given owner (for quota tracking). */
    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileEntity f WHERE f.owner = :owner")
    long sumSizeBytesByOwner(@Param("owner") User owner);

    /** Find all files belonging to a user (for account deletion). */
    List<FileEntity> findAllByOwner(User owner);

    /** Delete all files belonging to a user. */
    void deleteAllByOwner(User owner);
}
