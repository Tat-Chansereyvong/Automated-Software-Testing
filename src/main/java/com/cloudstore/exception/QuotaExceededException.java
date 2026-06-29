package com.cloudstore.exception;

/**
 * Thrown when an upload would exceed the user's storage quota.
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(long quotaBytes, long usedBytes, long fileSize) {
        super(String.format(
                "Quota exceeded: quota=%d bytes, used=%d bytes, file=%d bytes, need=%d more bytes",
                quotaBytes, usedBytes, fileSize, (usedBytes + fileSize) - quotaBytes));
    }
}
