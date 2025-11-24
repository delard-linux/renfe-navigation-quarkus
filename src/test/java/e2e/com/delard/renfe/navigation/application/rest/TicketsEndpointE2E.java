package com.delard.renfe.navigation.application.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * E2E tests that cover the `/tickets/purchase` endpoint.
 */
@QuarkusIntegrationTest
public class TicketsEndpointE2E {

  /**
   * Validates that `/tickets/purchase` returns 200 when valid data is provided.
   */
  @Test
  public void testPurchaseTicket() {
    Map<String, Object> request = new HashMap<>();
    request.put("origin", "OURENSE");
    request.put("destination", "MADRID (TODAS)");
    request.put("dateOut", "2025-12-01");
    request.put("adults", "1");
    request.put("userName", "John Doe");
    request.put("serviceType", "AVE");
    request.put("departureTime", "10:00");
    request.put("fareName", "Basica");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/tickets/purchase")
        .then()
        .statusCode(200)
        .body("message", notNullValue());
  }

  /** Validates that `/tickets/purchase` returns 400 when body is missing. */
  @Test
  public void testPurchaseTicketInvalid() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/tickets/purchase")
        .then()
        .statusCode(400);
  }
}


