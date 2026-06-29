package com.cloudstore;

import com.cloudstore.dto.RegisterRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the profile API endpoint using two testing methods:
 *   3. Regex Matched — email format matches a regex pattern
 *   9. Schema/JSON   — /api/me response has required fields
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("API")
@Feature("Profile")
public class ProfileApiTest {

    @LocalServerPort
    int port;

    static Playwright playwright;
    APIRequestContext request;
    APIRequestContext unauthRequest;

    private final String testEmail = "profile-api-" + System.nanoTime() + "@test.com";
    private final String testPassword = "Secret123!";

    @BeforeAll
    static void launchPlaywright() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void closePlaywright() {
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndUser() {
        String baseUrl = "http://localhost:" + port;

        // Unauthenticated context for registration
        unauthRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(baseUrl));

        // Register the test user
        unauthRequest.post("/api/auth/register",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData("{\"email\":\"" + testEmail + "\",\"password\":\"" + testPassword + "\"}"));

        // Authenticated context (HTTP Basic)
        String credentials = Base64.getEncoder().encodeToString((testEmail + ":" + testPassword).getBytes());
        request = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
                        .setExtraHTTPHeaders(Map.of("Authorization", "Basic " + credentials)));
    }

    @AfterEach
    void disposeContext() {
        if (request != null) request.dispose();
        if (unauthRequest != null) unauthRequest.dispose();
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 3: REGEX MATCHED
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Email format validation")
    @DisplayName("Regex — email in profile matches email pattern")
    void profileEmailMatchesRegex() {
        APIResponse res = Allure.step("GET /api/me", () ->
                request.get("/api/me"));

        Allure.step("Verify status 200", () -> {
            assertThat(res.status()).isEqualTo(200);
        });

        Allure.step("Verify email matches regex pattern", () -> {
            JsonObject body = JsonParser.parseString(res.text()).getAsJsonObject();
            String email = body.get("email").getAsString();

            assertThat(email)
                    .as("Email should match standard email format")
                    .matches("^[\\w.+-]+@[\\w.-]+$");
        });

        // Attach response JSON to Allure report
        Allure.addAttachment("GET /api/me response", "application/json", res.text());
    }

    // ───────────────────────────────────────────────────────────────
    //  METHOD 9: SCHEMA / JSON
    // ───────────────────────────────────────────────────────────────

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("API contract")
    @DisplayName("Schema — /api/me response contains required fields")
    void profileResponseHasRequiredFields() {
        APIResponse res = Allure.step("GET /api/me", () ->
                request.get("/api/me"));

        Allure.step("Verify status 200", () -> {
            assertThat(res.status()).isEqualTo(200);
        });

        Allure.step("Verify JSON schema has all required fields", () -> {
            JsonObject body = JsonParser.parseString(res.text()).getAsJsonObject();

            // Required fields
            assertThat(body.has("id")).as("Response must have 'id'").isTrue();
            assertThat(body.has("email")).as("Response must have 'email'").isTrue();
            assertThat(body.has("displayName")).as("Response must have 'displayName'").isTrue();
            assertThat(body.has("quotaBytes")).as("Response must have 'quotaBytes'").isTrue();
            assertThat(body.has("usedBytes")).as("Response must have 'usedBytes'").isTrue();
            assertThat(body.has("freeBytes")).as("Response must have 'freeBytes'").isTrue();

            // Type checks
            assertThat(body.get("id").isJsonPrimitive()).as("'id' should be a primitive").isTrue();
            assertThat(body.get("email").getAsString()).as("'email' should be non-empty").isNotEmpty();
            assertThat(body.get("quotaBytes").getAsLong())
                    .as("'quotaBytes' should be a positive number")
                    .isGreaterThan(0);
        });

        // Attach response JSON
        Allure.addAttachment("Profile JSON", "application/json", res.text());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("API contract")
    @DisplayName("Schema — quota values are consistent in JSON response")
    void quotaValuesConsistentInResponse() {
        APIResponse res = Allure.step("GET /api/me", () ->
                request.get("/api/me"));

        Allure.step("Verify quota consistency: free = quota - used", () -> {
            JsonObject body = JsonParser.parseString(res.text()).getAsJsonObject();
            long quota = body.get("quotaBytes").getAsLong();
            long used = body.get("usedBytes").getAsLong();
            long free = body.get("freeBytes").getAsLong();

            assertThat(free)
                    .as("freeBytes should equal quotaBytes - usedBytes")
                    .isEqualTo(quota - used);
        });
    }
}
