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
 * Pruebas E2E del recurso de trenes usando Playwright en modo no headless (debug).
 * Valida que la búsqueda de trenes retorne JSON con campos esperados.
 */
public class TrainResourceE2ETest {

    /**
     * Comprueba que el GET `/trains` responde 200 y contiene `trains_out`.
     * Usa parámetros de ejemplo para simular una búsqueda de ida.
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


