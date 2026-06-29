package com.cloudstore;

import com.cloudstore.exception.QuotaExceededException;
import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests quota management using four testing methods:
 *   1. Content Equals  — new user quota == 50 MB
 *   2. Formula Matched — freeBytes = quotaBytes − usedBytes
 *   3. Exception       — over-quota upload throws QuotaExceededException
 *   4. Tolerance       — usedBytes in MB ≈ expected value within delta
 */
@SpringBootTest
@Epic("Storage")
@Feature("Quota")
public class QuotaServiceTest {

    static final long MB = 1_048_576L;

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;

    private User user;
    private int counter = 0;

    @BeforeEach
    void setUp() {
        // Create a fresh user for each test with a unique email
        counter++;
        user = userService.register("quota-test-" + counter + "-" + System.nanoTime() + "@test.com", "Secret123!");
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 1: CONTENT EQUALS
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("New user gets 50 MB")
    @DisplayName("Equals — new user quota is exactly 50 MB (52 428 800 bytes)")
    void newUserQuotaEquals50MB() {
        Allure.step("Verify default quota", () -> {
            assertThat(user.getQuotaBytes())
                    .as("Default quota should be exactly 50 MB")
                    .isEqualTo(50 * MB);
        });

        Allure.step("Verify initial used bytes is zero", () -> {
            assertThat(user.getUsedBytes())
                    .as("New user should have 0 used bytes")
                    .isEqualTo(0L);
        });
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 4: FORMULA MATCHED
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Quota formula")
    @DisplayName("Formula — free = quota − Σ(file sizes)")
    void quotaFormulaHolds() {
        long file1Size = 1024;  // 1 KB
        long file2Size = 2048;  // 2 KB

        Allure.step("Upload first file (" + file1Size + " bytes)", () -> {
            storageService.uploadFile(user, "file1.txt", new byte[(int) file1Size], null);
        });

        Allure.step("Upload second file (" + file2Size + " bytes)", () -> {
            storageService.uploadFile(user, "file2.txt", new byte[(int) file2Size], null);
        });

        Allure.step("Verify formula: free = quota − used", () -> {
            long expectedFree = user.getQuotaBytes() - (file1Size + file2Size);
            assertThat(storageService.freeBytes(user))
                    .as("freeBytes should equal quotaBytes minus sum of uploaded file sizes")
                    .isEqualTo(expectedFree);
        });
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 7: EXCEPTION
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Over-quota rejection")
    @DisplayName("Exception — upload exceeding quota throws QuotaExceededException")
    void overQuotaUploadThrowsException() {
        Allure.step("Set quota to 1 KB for testing", () -> {
            user.setQuotaBytes(1024L);
        });

        Allure.step("Upload a small file (500 bytes) — should succeed", () -> {
            storageService.uploadFile(user, "small.txt", new byte[500], null);
        });

        Allure.step("Attempt to upload a file exceeding remaining quota — should throw", () -> {
            assertThrows(QuotaExceededException.class, () ->
                    storageService.uploadFile(user, "big.txt", new byte[600], null),
                    "Uploading 600 bytes when only ~524 bytes remain should throw QuotaExceededException"
            );
        });
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 8: TOLERANCE
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Tolerance check")
    @DisplayName("Tolerance — used MB ≈ expected value within delta")
    void usedBytesToleranceCheck() {
        long fileSize = 5000; // 5000 bytes

        Allure.step("Upload a " + fileSize + "-byte file", () -> {
            storageService.uploadFile(user, "data.bin", new byte[(int) fileSize], null);
        });

        Allure.step("Verify used MB is within tolerance", () -> {
            double usedMB = user.getUsedBytes() / (double) MB;
            double expectedMB = fileSize / (double) MB;
            assertThat(usedMB)
                    .as("Used MB should be approximately %.6f within 0.01 delta", expectedMB)
                    .isCloseTo(expectedMB, within(0.01));
        });
    }

    // ───────────────────────────────────────────────────────────────
    //  COMBINED: equals + formula + exception (matches lab example)
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Combined quota scenario")
    @DisplayName("Combined — equals + formula + exception in one scenario")
    void combinedQuotaScenario() {
        Allure.step("Verify quota equals 50 MB", () -> {
            assertThat(user.getQuotaBytes()).isEqualTo(50 * MB);
        });

        // Use small quota for faster testing
        user.setQuotaBytes(10_000L);

        Allure.step("Upload 4000 bytes", () -> {
            storageService.uploadFile(user, "a.txt", new byte[4000], null);
        });

        Allure.step("Upload 3000 bytes", () -> {
            storageService.uploadFile(user, "b.txt", new byte[3000], null);
        });

        Allure.step("Verify formula holds", () -> {
            long expectedFree = 10_000L - 7000L;
            assertThat(storageService.freeBytes(user)).isEqualTo(expectedFree);
        });

        Allure.step("Verify over-quota throws exception", () -> {
            assertThrows(QuotaExceededException.class, () ->
                    storageService.uploadFile(user, "c.txt", new byte[4000], null));
        });
    }
}
