package com.delard.renfe.navigation.infrastructure.service;

import java.net.URI;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PlaywrightSearchTrainsService using the REST endpoint.
 * Uses @QuarkusIntegrationTest to test against the packaged application.
 */
@QuarkusIntegrationTest
class PlaywrightSearchTrainsServiceIT {

    private static final Logger LOG = Logger.getLogger(PlaywrightSearchTrainsServiceIT.class);

    @TestHTTPResource
    URI baseUri;

    @Test
    void shouldRetrieveOutboundTrainsFromRenfe() {
        System.out.println("🚀 Quarkus test server running at: " + baseUri);
        Response response = given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-15")
            .queryParam("adults", 1)
        .when()
            .get("/trains")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();

        String responseBody = response.getBody().asString();
        LOG.infof("E2E response: %s", responseBody);

        // Validate response structure
        assertNotNull(responseBody);
        assertFalse(responseBody.isEmpty(), "Response body should not be empty");

        // Validate JSON structure
        response.then()
            .body("trains_out", notNullValue())
            .body("trains_out.size()", greaterThan(0));

        LOG.infof("E2E outbound trains count: %d", 
            response.jsonPath().getList("trains_out").size());
    }
}


