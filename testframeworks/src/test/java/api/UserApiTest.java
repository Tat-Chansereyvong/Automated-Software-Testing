package api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

public class UserApiTest {

    private static WireMockServer wireMockServer;
    private String apiKey;

    @BeforeClass
    public void setupApi() {
        apiKey = System.getenv("REQRES_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("reqres.api.key");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            // Start WireMock and stub responses so tests can run offline without API key
            wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMockServer.start();
            configureFor("localhost", wireMockServer.port());

            String user2 = "{\n" +
                    "  \"data\": {\n" +
                    "    \"id\": 2,\n" +
                    "    \"email\": \"janet.weaver@reqres.in\",\n" +
                    "    \"first_name\": \"Janet\",\n" +
                    "    \"last_name\": \"Weaver\",\n" +
                    "    \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
                    "  },\n" +
                    "  \"support\": {\n" +
                    "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                    "    \"text\": \"To keep ReqRes free, contributions towards server costs are appreciated.\"\n" +
                    "  }\n" +
                    "}";

            String user3 = "{\n" +
                    "  \"data\": {\n" +
                    "    \"id\": 3,\n" +
                    "    \"email\": \"emma.wong@reqres.in\",\n" +
                    "    \"first_name\": \"Emma\",\n" +
                    "    \"last_name\": \"Wong\",\n" +
                    "    \"avatar\": \"https://reqres.in/img/faces/3-image.jpg\"\n" +
                    "  },\n" +
                    "  \"support\": {\n" +
                    "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                    "    \"text\": \"To keep ReqRes free, contributions towards server costs are appreciated.\"\n" +
                    "  }\n" +
                    "}";

            stubFor(get(urlEqualTo("/api/users/2"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(user2)));

            stubFor(get(urlEqualTo("/api/users/3"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(user3)));

            RestAssured.baseURI = "http://localhost:" + wireMockServer.port() + "/api";
        } else {
            RestAssured.baseURI = "https://reqres.in/api";
        }
    }

    @AfterClass
    public void teardownApi() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @DataProvider(name = "userData")
    public Object[][] getUserData() {
        return new Object[][] {
                { 2, "Janet" },
                { 3, "Emma" }
        };
    }

    @Test(dataProvider = "userData")
    public void testGetUserDetails(int userId, String expectedFirstName) {

        io.restassured.specification.RequestSpecification req = given().log().all();
        if (apiKey != null && !apiKey.isEmpty()) {
            req.header("x-api-key", apiKey);
        }

        req.when()
                .get("/users/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(2000L)) // Performance check
                .body("data.first_name", equalTo(expectedFirstName))
                // Schema Validation
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }
}