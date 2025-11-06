package com.delard.renfe.navigation.application.rest;

import com.delard.renfe.navigation.support.config.PlaywrightDebugNoHeadlessProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestProfile(PlaywrightDebugNoHeadlessProfile.class)
/**
 * Integration tests for train resource.
 * Validates correct responses and common validation errors.
 */
public class TrainResourceIT {

    /**
     * Validates that `/trains` returns 200 and JSON with expected basic fields
     * when valid parameters are provided for an outbound search.
     */
    @Test
    public void testGetTrainsEndpoint() {
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .queryParam("adults", 1)
            .when().get("/trains")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("origin", is("OURENSE"))
            .body("destination", is("MADRID"))
            .body("date_out", is("2025-12-01"))
            .body("adults", is(1))
            .body("trains_out", notNullValue());
    }

    /**
     * Validates that `/trains` accepts return date and returns 200
     * with outbound fields and return date present.
     */
    @Test
    public void testGetTrainsWithReturnDate() {
        given()
            .queryParam("origin", "BARCELONA")
            .queryParam("destination", "VALENCIA")
            .queryParam("date_out", "2025-12-15")
            .queryParam("date_return", "2025-12-20")
            .queryParam("adults", 2)
            .when().get("/trains")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("origin", is("BARCELONA"))
            .body("destination", is("VALENCIA"))
            .body("date_return", is("2025-12-20"))
            .body("adults", is(2));
    }

    /**
     * Verifies that if `origin` is missing, the endpoint responds with 400 (invalid request).
     */
    @Test
    public void testGetTrainsMissingOrigin() {
        given()
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .when().get("/trains")
            .then()
            .statusCode(400);
    }

    /**
     * Verifies that an invalid date format in `date_out` causes a 400 error.
     */
    @Test
    public void testGetTrainsInvalidDateFormat() {
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "01-12-2025")
            .when().get("/trains")
            .then()
            .statusCode(400);
    }

    /**
     * Verifies that a number of adults out of range returns a 400 error.
     */
    @Test
    public void testGetTrainsInvalidAdultsCount() {
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .queryParam("adults", 10)
            .when().get("/trains")
            .then()
            .statusCode(400);
    }

    /**
     * Verifies that the `/trains` endpoint returns valid results when performing
     * a real search with Playwright. Also prints the formatted response
     * to facilitate manual inspection in test logs (useful for debugging).
     */
    @Test
    public void testGetTrainsWithFormattedOutput() {
        Response resp = given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .queryParam("adults", 1)
        .when().get("/trains")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("trains_out", notNullValue())
        .extract().response();

        // Pretty print the JSON response for human verification in test logs
        try {
            String pretty = resp.prettyPrint();
            System.out.println("\n===== TRAIN SEARCH RESPONSE (pretty) =====\n" + pretty + "\n========================================\n");
        } catch (Exception e) {
            System.out.println("Could not pretty-print response: " + e.getMessage());
            System.out.println(resp.asString());
        }
    }
}


