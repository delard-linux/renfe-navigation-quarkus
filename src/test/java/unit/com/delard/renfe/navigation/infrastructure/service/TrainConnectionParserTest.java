/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.service;


import static org.junit.jupiter.api.Assertions.*;

import com.delard.renfe.navigation.domain.model.TrainConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Unit tests for TrainConnectionParser
 */
@ExtendWith(MockitoExtension.class)
class TrainConnectionParserTest
{

    private TrainConnectionParser parser;

    @BeforeEach
    void setUp()
    {
        parser = new TrainConnectionParser();
    }

    @Test
    @DisplayName("parseTrainConnection should return null when reorder-trenes-enlaces div is missing")
    void testParseTrainConnectionMissingDiv()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should return null when aria-hidden is true")
    void testParseTrainConnectionWithAriaHidden()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8" aria-hidden="true">
                        <div class="col-md-8 principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should return null when enlace-tren span is missing")
    void testParseTrainConnectionMissingEnlaceSpan()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md-8 principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should return null when duration is missing")
    void testParseTrainConnectionMissingDuration()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren REG.EXP." />
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should return null when first train type is missing")
    void testParseTrainConnectionMissingFirstTrainType()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                            <span class="enlace-tren-min">1 horas 10 minutos</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should return null when second train type is missing")
    void testParseTrainConnectionMissingSecondTrainType()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren REG.EXP." />
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                            <span class="enlace-tren-min">1 horas 10 minutos</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should extract complete connection information")
    void testParseTrainConnectionComplete()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <div class="trenes-enlaces">
                                <img alt="Imagen de Tren. Tipo de tren REG.EXP." />
                            </div>
                        </div>
                        <div>
                            <span class="enlace-tren" aria-label="Tren enlazado">Enlace</span>
                            <hr class="linea-divisoria-enlace" />
                            <span class="enlace-tren-min" aria-label="Duracion de transbordo">1 horas 10 minutos</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <div class="trenes-enlaces">
                                <img alt="Imagen de Tren. Tipo de tren AVE" />
                            </div>
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNotNull(connection);
        assertEquals("1 horas 10 minutos", connection.getDuration());
        assertEquals("REG.EXP.", connection.getFirstTrainType());
        assertEquals("AVE", connection.getSecondTrainType());
    }

    @Test
    @DisplayName("parseTrainConnection should handle different train types")
    void testParseTrainConnectionDifferentTrainTypes()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren ALVIA" />
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                            <span class="enlace-tren-min">45 minutos</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <img alt="Imagen de Tren. Tipo de tren EUROMED" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNotNull(connection);
        assertEquals("45 minutos", connection.getDuration());
        assertEquals("ALVIA", connection.getFirstTrainType());
        assertEquals("EUROMED", connection.getSecondTrainType());
    }

    @Test
    @DisplayName("parseTrainConnection should handle different duration formats")
    void testParseTrainConnectionDifferentDurations()
    {
        String[] durations = { "1 hora", "2 horas", "30 minutos", "1 hora 5 minutos" };

        for (String duration : durations) {
            String html = String.format("""
                    <div class="selectedTren" role="listitem" id="tren_i_1">
                        <div class="reorder-trenes-enlaces col-lg-8">
                            <div class="col-md principal-tren-enlace">
                                <img alt="Imagen de Tren. Tipo de tren AVE" />
                            </div>
                            <div>
                                <span class="enlace-tren">Enlace</span>
                                <span class="enlace-tren-min">%s</span>
                            </div>
                            <div class="col-md principal-tren-enlace-2">
                                <img alt="Imagen de Tren. Tipo de tren ALVIA" />
                            </div>
                        </div>
                    </div>
                    """, duration);
            Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

            TrainConnection connection = parser.parseTrainConnection(row, "i_1");

            assertNotNull(connection, "Connection should be found for duration: " + duration);
            assertEquals(duration, connection.getDuration());
        }
    }

    @Test
    @DisplayName("parseTrainConnection should return null when duration is empty")
    void testParseTrainConnectionEmptyDuration()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                            <span class="enlace-tren-min"></span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <img alt="Imagen de Tren. Tipo de tren ALVIA" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNull(connection);
    }

    @Test
    @DisplayName("parseTrainConnection should handle train types with special characters")
    void testParseTrainConnectionWithSpecialCharacters()
    {
        String html = """
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <div class="reorder-trenes-enlaces col-lg-8">
                        <div class="col-md principal-tren-enlace">
                            <img alt="Imagen de Tren. Tipo de tren REG.EXP." />
                        </div>
                        <div>
                            <span class="enlace-tren">Enlace</span>
                            <span class="enlace-tren-min">1 hora</span>
                        </div>
                        <div class="col-md principal-tren-enlace-2">
                            <img alt="Imagen de Tren. Tipo de tren AVE" />
                        </div>
                    </div>
                </div>
                """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");

        TrainConnection connection = parser.parseTrainConnection(row, "i_1");

        assertNotNull(connection);
        assertEquals("REG.EXP.", connection.getFirstTrainType());
        assertEquals("AVE", connection.getSecondTrainType());
    }
}
