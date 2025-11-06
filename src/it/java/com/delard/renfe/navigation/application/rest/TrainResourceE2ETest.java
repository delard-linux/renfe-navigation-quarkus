package com.delard.renfe.navigation.application.rest;

import com.delard.renfe.navigation.support.config.PlaywrightDebugNoHeadlessProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestProfile(PlaywrightDebugNoHeadlessProfile.class)
/**
 * E2E tests for train resource using Playwright in non-headless mode (debug).
 * Validates that train search returns JSON with expected fields.
 */
public class TrainResourceE2ETest {

    /**
     * Checks that GET `/trains` responds with 200 and contains `trains_out`.
     * Uses example parameters to simulate an outbound search.
     */
    @Test
    public void e2eGetTrainsNonHeadless() {
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .queryParam("adults", 1)
        .when().get("/trains")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("trains_out", notNullValue());
    }
}


