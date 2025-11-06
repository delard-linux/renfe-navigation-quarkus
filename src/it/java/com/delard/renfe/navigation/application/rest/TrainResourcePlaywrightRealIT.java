package com.delard.renfe.navigation.application.rest;

import com.delard.renfe.navigation.support.config.PlaywrightDebugNoHeadlessProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestProfile(PlaywrightDebugNoHeadlessProfile.class)
class TrainResourcePlaywrightRealIT {

    /**
     * Verifica que el endpoint `/trains` devuelve resultados válidos cuando se realiza
     * una búsqueda real con Playwright en modo headless. También imprime la respuesta
     * formateada para facilitar la inspección manual en los logs del test.
     */
    @Test
    void shouldReturnTrainsWhenSearchingWithPlaywrightTest() {
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


