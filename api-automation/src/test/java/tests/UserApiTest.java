package test.java.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@Feature("User Management API")
public class UserApiTest {

    private static RequestSpecification requestSpec;

    @BeforeAll
    public static void setup() {
        // Base setup for all tests
        RestAssured.baseURI = "https://reqres.in/api";

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                // ADDED THIS: The required authentication header to fix the 401 error
                .addHeader("x-api-key", "free_user_3DCZns64pHDsxKtNYIao6xefJov")
                .build();
    }

    @Test
    @Description("Verify GET User API returns 200 and matches the expected JSON Schema")
    public void getUser_ValidatesSchemaAndData() {
        given()
                .spec(requestSpec)
                // Log request details to the report/console
                .log().all()
                .when()
                .get("/users/2")
                .then()
                // Log response details
                .log().all()
                // Assert Status Code
                .statusCode(200)
                // Assert specific data points
                .body("data.first_name", equalTo("Janet"))
                // Assert JSON Schema Validation
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }
}