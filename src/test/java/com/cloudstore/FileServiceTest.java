package com.cloudstore;

import com.cloudstore.model.FileEntity;
import com.cloudstore.model.Folder;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests file operations using the CONTAINS testing method:
 *   2. Contains — folder listing contains the uploaded file's name.
 */
@SpringBootTest
@Epic("Storage")
@Feature("Files")
public class FileServiceTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userService.register("file-test-" + System.nanoTime() + "@test.com", "Secret123!");
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 2: CONTAINS
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("File listing")
    @DisplayName("Contains — file listing contains the uploaded file name")
    void fileListingContainsUploadedFileName() {
        Allure.step("Upload 'report.pdf' to root", () -> {
            storageService.uploadFile(user, "report.pdf", "PDF content".getBytes(), null);
        });

        Allure.step("Upload 'notes.txt' to root", () -> {
            storageService.uploadFile(user, "notes.txt", "Some notes".getBytes(), null);
        });

        Allure.step("Verify listing contains uploaded file names", () -> {
            List<String> fileNames = storageService.listFiles(user, null).stream()
                    .map(FileEntity::getName)
                    .collect(Collectors.toList());

            assertThat(fileNames)
                    .as("Root file listing should contain the uploaded file names")
                    .contains("report.pdf", "notes.txt");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("File in folder")
    @DisplayName("Contains — file inside a folder appears in that folder's listing")
    void fileInFolderContainsInListing() {
        Folder docs = Allure.step("Create 'Documents' folder", () ->
                storageService.createFolder(user, "Documents", null));

        Allure.step("Upload 'homework.docx' into Documents", () -> {
            storageService.uploadFile(user, "homework.docx", "Doc content".getBytes(),
                    "application/msword", docs.getId());
        });

        Allure.step("Verify Documents listing contains 'homework.docx'", () -> {
            List<String> fileNames = storageService.listFiles(user, docs.getId()).stream()
                    .map(FileEntity::getName)
                    .collect(Collectors.toList());

            assertThat(fileNames)
                    .as("Documents folder should contain 'homework.docx'")
                    .contains("homework.docx");
        });

        Allure.step("Verify root listing does NOT contain 'homework.docx'", () -> {
            List<String> rootFiles = storageService.listFiles(user, null).stream()
                    .map(FileEntity::getName)
                    .collect(Collectors.toList());

            assertThat(rootFiles)
                    .as("Root listing should NOT contain file uploaded to Documents")
                    .doesNotContain("homework.docx");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Folder listing")
    @DisplayName("Contains — folder listing contains created folder name")
    void folderListingContainsCreatedFolder() {
        Allure.step("Create 'Photos' folder", () -> {
            storageService.createFolder(user, "Photos", null);
        });

        Allure.step("Create 'Music' folder", () -> {
            storageService.createFolder(user, "Music", null);
        });

        Allure.step("Verify listing contains 'Photos' and 'Music'", () -> {
            List<String> folderNames = storageService.listFolders(user, null).stream()
                    .map(Folder::getName)
                    .collect(Collectors.toList());

            assertThat(folderNames)
                    .as("Root folder listing should contain created folder names")
                    .contains("Photos", "Music");
        });
    }
}
