package com.renfe.navigation.infrastructure.adapter.input.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestProfile(PlaywrightE2eProfile.class)
public class TrainResourceE2ETest {

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

