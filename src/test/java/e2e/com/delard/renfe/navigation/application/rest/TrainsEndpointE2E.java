package com.delard.renfe.navigation.application.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * E2E tests that cover the `/trains` endpoint with different scenarios.
 */
@QuarkusIntegrationTest
public class TrainsEndpointE2E {

  private static final Logger LOG = Logger.getLogger(TrainsEndpointE2E.class);

  private static final int HTTP_TIMEOUT_MS = 120_000;
  private static RestAssuredConfig customConfig;

  /**
   * Calculates the outbound date (2 months from today) in format yyyy-MM-dd for REST API.
   *
   * @return Outbound date in format yyyy-MM-dd
   */
  private String calculateOutboundDate() {
    LocalDate today = LocalDate.now();
    LocalDate outboundDate = today.plusMonths(2);
    return outboundDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  /**
   * Calculates the outbound date (2 months from today) in format dd/MM/yyyy for response validation.
   *
   * @return Outbound date in format dd/MM/yyyy
   */
  private String calculateOutboundDateResponseFormat() {
    LocalDate today = LocalDate.now();
    LocalDate outboundDate = today.plusMonths(2);
    return outboundDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  }

  /**
   * Calculates the return date (3 days after the outbound date) in format yyyy-MM-dd for REST API.
   *
   * @param outboundDate Outbound date in format yyyy-MM-dd
   * @return Return date in format yyyy-MM-dd
   */
  private String calculateReturnDate(String outboundDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate outbound = LocalDate.parse(outboundDate, formatter);
    LocalDate returnDate = outbound.plusDays(3);
    return returnDate.format(formatter);
  }

  /**
   * Calculates the return date (3 days after the outbound date) in format dd/MM/yyyy for response validation.
   *
   * @param outboundDate Outbound date in format dd/MM/yyyy
   * @return Return date in format dd/MM/yyyy
   */
  private String calculateReturnDateResponseFormat(String outboundDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDate outbound = LocalDate.parse(outboundDate, formatter);
    LocalDate returnDate = outbound.plusDays(3);
    return returnDate.format(formatter);
  }

  @BeforeAll
  static void configureHttpClientTimeouts() {
    // Configure HTTP client with extended timeouts for long-running Playwright operations
    // Using setParam with the correct parameter names for Apache HttpClient 4.5.x
    customConfig =
        RestAssuredConfig.config()
            .httpClient(
                HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", HTTP_TIMEOUT_MS)
                    .setParam("http.socket.timeout", HTTP_TIMEOUT_MS)
                    .setParam("http.connection-manager.timeout", HTTP_TIMEOUT_MS));

    LOG.infof("Configured HTTP client timeouts: %d ms", HTTP_TIMEOUT_MS);
  }

  /**
   * Validates that `/trains` returns 200 and JSON with expected basic fields when valid
   * parameters are provided for an outbound search.
   */
  @Test
  public void testGetTrainsEndpoint() {
    String dateOut = calculateOutboundDate();
    String dateOutResponseFormat = calculateOutboundDateResponseFormat();
    
    given()
        .queryParam("origin", "OURENSE")
        .queryParam("destination", "MADRID (TODAS)")
        .queryParam("date_out", dateOut)
        .queryParam("adults", 1)
        .when()
        .get("/trains")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("origin", is("OURENSE"))
        .body("destination", is("MADRID (TODAS)"))
        .body("date_out", is(dateOutResponseFormat))
        .body("adults", is("1"))
        .body("trains_out", notNullValue());
  }

  /**
   * Validates that `/trains` accepts return date and returns 200 with outbound fields and return
   * date present.
   */
  @Test
  public void testGetTrainsWithReturnDate() {
    String dateOut = calculateOutboundDate();
    String dateOutResponseFormat = calculateOutboundDateResponseFormat();
    String dateReturn = calculateReturnDate(dateOut);
    String dateReturnResponseFormat = calculateReturnDateResponseFormat(dateOutResponseFormat);
    
    given()
        .config(customConfig)
        .queryParam("origin", "OURENSE")
        .queryParam("destination", "MADRID (TODAS)")
        .queryParam("date_out", dateOut)
        .queryParam("date_return", dateReturn)
        .queryParam("adults", 2)
        .when()
        .get("/trains")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("origin", is("OURENSE"))
        .body("destination", is("MADRID (TODAS)"))
        .body("date_return", is(dateReturnResponseFormat))
        .body("adults", is("2"));
  }

  /**
   * Verifies that if `origin` is missing, the endpoint responds with 400 (invalid request).
   */
  @Test
  public void testGetTrainsMissingOrigin() {
    String dateOut = calculateOutboundDate();
    
    given()
        .queryParam("destination", "MADRID (TODAS)")
        .queryParam("date_out", dateOut)
        .when()
        .get("/trains")
        .then()
        .statusCode(400);
  }

  /** Verifies that an invalid date format in `date_out` causes a 400 error. */
  @Test
  public void testGetTrainsInvalidDateFormat() {
    given()
        .queryParam("origin", "OURENSE")
        .queryParam("destination", "MADRID (TODAS)")
        .queryParam("date_out", "01-12-2025")
        .when()
        .get("/trains")
        .then()
        .statusCode(400);
  }

  /** Verifies that a number of adults out of range returns a 400 error. */
  @Test
  public void testGetTrainsInvalidAdultsCount() {
    String dateOut = calculateOutboundDate();
    
    given()
        .queryParam("origin", "OURENSE")
        .queryParam("destination", "MADRID (TODAS)")
        .queryParam("date_out", dateOut)
        .queryParam("adults", 10)
        .when()
        .get("/trains")
        .then()
        .statusCode(400);
  }

  /**
   * Verifies that the `/trains` endpoint returns valid results when performing a real search with
   * Playwright. Also prints the formatted response to facilitate manual inspection in test logs
   * (useful for debugging).
   */
  @Test
  public void testGetTrainsWithFormattedOutput() {
    String dateOut = calculateOutboundDate();
    
    Response response =
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID (TODAS)")
            .queryParam("date_out", dateOut)
            .queryParam("adults", 1)
            .when()
            .get("/trains")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("trains_out", notNullValue())
            .extract()
            .response();

    assertNotNull(response);
  }

  @Test
  void shouldRetrieveOutboundTrainsFromRenfe() {
    String dateOut = calculateOutboundDate();
    
    Response response =
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID (TODAS)")
            .queryParam("date_out", dateOut)
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

    assertNotNull(responseBody);
    assertFalse(responseBody.isEmpty(), "Response body should not be empty");

    response
        .then()
        .body("trains_out", notNullValue())
        .body("trains_out.size()", greaterThan(0));

    LOG.infof(
        "E2E outbound trains count: %d", response.jsonPath().getList("trains_out").size());
  }
}


