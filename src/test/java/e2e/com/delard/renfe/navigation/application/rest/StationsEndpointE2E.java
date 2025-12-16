package com.delard.renfe.navigation.application.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * E2E tests that cover the `/stations` endpoint.
 */
@QuarkusIntegrationTest
public class StationsEndpointE2E {

  /**
   * Validates that `/stations` returns 200 and a list of stations when a valid search term is
   * provided.
   */
  @Test
  public void testGetStations() {
    given()
        .queryParam("search", "madrid")
        .when()
        .get("/stations")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", greaterThan(0));
  }

  /** Validates that `/stations` returns 400 when search term is too short. */
  @Test
  public void testGetStationsInvalid() {
    given()
        .queryParam("search", "ma")
        .when()
        .get("/stations")
        .then()
        .statusCode(400);
  }
}


