package com.cloudstore.exception;

/**
 * Thrown when a requested resource (file, folder, user) does not exist
 * or does not belong to the authenticated user.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}
