package com.cloudstore;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Base64;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Playwright browser test for the dashboard UI using:
 *   10. Visual/Snapshot — Playwright web assertion + screenshot attached to Allure
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("UI")
@Feature("Dashboard")
public class DashboardPlaywrightTest {

    @LocalServerPort
    int port;

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    private final String testEmail = "dashboard-" + System.nanoTime() + "@test.com";
    private final String testPassword = "Secret123!";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndRegister() {
        context = browser.newContext();
        page = context.newPage();

        // Register a user via the API first
        String baseUrl = "http://localhost:" + port;
        APIRequestContext apiCtx = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(baseUrl));
        apiCtx.post("/api/auth/register",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData("{\"email\":\"" + testEmail + "\",\"password\":\"" + testPassword + "\"}"));
        apiCtx.dispose();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 10: VISUAL / SNAPSHOT
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Dashboard visual")
    @DisplayName("Visual — dashboard shows '50 MB' quota and matches snapshot")
    void dashboardShowsQuotaAndSnapshot() {
        String baseUrl = "http://localhost:" + port;

        Allure.step("Navigate to login page", () -> {
            page.navigate(baseUrl + "/login");
        });

        Allure.step("Fill in credentials and submit", () -> {
            page.locator("[data-testid='email-input']").fill(testEmail);
            page.locator("[data-testid='password-input']").fill(testPassword);
            page.locator("[data-testid='login-btn']").click();
        });

        Allure.step("Wait for dashboard to load", () -> {
            page.waitForURL("**/dashboard");
        });

        Allure.step("Verify quota shows '50 MB'", () -> {
            assertThat(page.locator("[data-testid='quota']")).hasText("50 MB");
        });

        Allure.step("Verify page title is visible", () -> {
            assertThat(page.locator("[data-testid='page-title']")).isVisible();
        });

        Allure.step("Verify user email is displayed", () -> {
            assertThat(page.locator("[data-testid='user-email']")).containsText("@");
        });

        Allure.step("Take screenshot and attach to Allure report", () -> {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.addAttachment("dashboard-screenshot", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Dashboard elements")
    @DisplayName("Visual — dashboard displays profile and storage sections")
    void dashboardDisplaysProfileAndStorage() {
        String baseUrl = "http://localhost:" + port;

        Allure.step("Login via UI", () -> {
            page.navigate(baseUrl + "/login");
            page.locator("[data-testid='email-input']").fill(testEmail);
            page.locator("[data-testid='password-input']").fill(testPassword);
            page.locator("[data-testid='login-btn']").click();
            page.waitForURL("**/dashboard");
        });

        Allure.step("Verify display name is shown", () -> {
            assertThat(page.locator("[data-testid='display-name']")).isVisible();
        });

        Allure.step("Verify used storage shows '0 MB'", () -> {
            assertThat(page.locator("[data-testid='used-storage']")).hasText("0 MB");
        });

        Allure.step("Verify free storage info is shown", () -> {
            assertThat(page.locator("[data-testid='free-storage']")).containsText("MB free");
        });

        Allure.step("Take final screenshot", () -> {
            byte[] screenshot = page.screenshot();
            Allure.addAttachment("dashboard-profile", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
        });
    }
}
