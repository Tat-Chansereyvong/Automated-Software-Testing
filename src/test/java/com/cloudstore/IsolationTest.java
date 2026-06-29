package com.cloudstore;

import com.cloudstore.model.User;
import com.cloudstore.service.StorageService;
import com.cloudstore.service.UserService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests user isolation using two testing methods:
 *   5. Predicate   — usedBytes(userB) == 0 after userA uploads
 *   6. Collection  — userB's file list is empty, has no duplicates, correct size
 */
@SpringBootTest
@Epic("Security")
@Feature("Isolation")
public class IsolationTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        long ts = System.nanoTime();
        userA = userService.register("alice-" + ts + "@test.com", "pwA");
        userB = userService.register("bob-" + ts + "@test.com", "pwB");
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 5: PREDICATE
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("User data isolation")
    @DisplayName("Predicate — userB uses 0 bytes after userA uploads")
    void userBUsesNothingAfterUserAUploads() {
        Allure.step("UserA uploads 'secret.txt'", () -> {
            storageService.uploadFile(userA, "secret.txt",
                    "Top secret data".getBytes(), null);
        });

        Allure.step("Verify predicate: userB uses nothing", () -> {
            assertThat(userB)
                    .as("UserB should have used 0 bytes — userA's upload must not affect userB")
                    .matches(u -> storageService.usedBytes(u) == 0, "usedBytes == 0");
        });
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 6: COLLECTION
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("File list isolation")
    @DisplayName("Collection — userB's file list is empty and doesn't contain userA's files")
    void userBCannotSeeUserAFiles() {
        Allure.step("UserA uploads 'secret.txt'", () -> {
            storageService.uploadFile(userA, "secret.txt",
                    "Top secret data".getBytes(), null);
        });

        Allure.step("UserA uploads 'confidential.pdf'", () -> {
            storageService.uploadFile(userA, "confidential.pdf",
                    "Confidential".getBytes(), null);
        });

        Allure.step("Verify userB's file list is empty", () -> {
            List<String> bFiles = storageService.listAllFileNames(userB);

            assertThat(bFiles)
                    .as("UserB should have an empty file list")
                    .isEmpty();

            assertThat(bFiles)
                    .as("UserB's list must NOT contain userA's files")
                    .doesNotContain("secret.txt", "confidential.pdf");
        });
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Folder isolation")
    @DisplayName("Collection — userB cannot see userA's folders")
    void userBCannotSeeUserAFolders() {
        Allure.step("UserA creates 'Private' folder", () -> {
            storageService.createFolder(userA, "Private", null);
        });

        Allure.step("Verify userB sees no folders", () -> {
            assertThat(storageService.listFolders(userB, null))
                    .as("UserB should see zero folders")
                    .isEmpty();
        });

        Allure.step("Verify userB's folder count is 0 (collection size)", () -> {
            assertThat(storageService.listFolders(userB, null))
                    .as("UserB's folder collection should have size 0")
                    .hasSize(0);
        });
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("No cross-user data leaks")
    @DisplayName("Predicate + Collection — combined isolation check")
    void combinedIsolationCheck() {
        Allure.step("UserA uploads multiple files", () -> {
            storageService.uploadFile(userA, "doc1.txt", new byte[100], null);
            storageService.uploadFile(userA, "doc2.txt", new byte[200], null);
            storageService.createFolder(userA, "Work", null);
        });

        Allure.step("Verify userA has 2 files and 1 folder", () -> {
            assertThat(storageService.listAllFileNames(userA)).hasSize(2);
            assertThat(storageService.listFolders(userA, null)).hasSize(1);
        });

        Allure.step("Verify userB has nothing (collection)", () -> {
            List<String> bFiles = storageService.listAllFileNames(userB);
            assertThat(bFiles).isEmpty();
            assertThat(bFiles).doesNotContain("doc1.txt", "doc2.txt");
        });

        Allure.step("Verify userB predicate: usedBytes == 0", () -> {
            assertThat(userB).matches(
                    u -> storageService.usedBytes(u) == 0,
                    "UserB should use 0 bytes");
        });
    }
}
