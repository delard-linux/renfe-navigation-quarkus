package com.delard.renfe.navigation.infrastructure.adapter.input.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class TrainResourceTest {

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

    @Test
    public void testGetTrainsMissingOrigin() {
        given()
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .when().get("/trains")
            .then()
            .statusCode(400);
    }

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

    @Test
    public void testGetTrainsFlowEndpoint() {
        given()
            .queryParam("origin", "OURENSE")
            .queryParam("destination", "MADRID")
            .queryParam("date_out", "2025-12-01")
            .queryParam("adults", 1)
            .when().get("/trains-flow")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", notNullValue())
            .body("filepath", notNullValue());
    }

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
}

