package com.cloudstore.repository;

import com.cloudstore.model.Folder;
import com.cloudstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    /** List folders owned by user at a given nesting level (null parent = root). */
    List<Folder> findByOwnerAndParent(User owner, Folder parent);

    /** List all root-level folders (parent is null) for a given owner. */
    List<Folder> findByOwnerAndParentIsNull(User owner);

    /** Find a folder by id only if it belongs to the given owner (isolation). */
    Optional<Folder> findByIdAndOwner(Long id, User owner);

    /** Delete all folders belonging to a user (for account deletion). */
    void deleteAllByOwner(User owner);
}
