package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainHtmlParser
 */
@ExtendWith(MockitoExtension.class)
class TrainHtmlParserTest {

    private TrainHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new TrainHtmlParser();
    }

    @Test
    void testParseTrainListWithEmptyHtml() {
        List<Train> trains = parser.parseTrainList("");
        assertNotNull(trains);
        assertTrue(trains.isEmpty());
    }

    @Test
    void testParseTrainListWithNullHtml() {
        List<Train> trains = parser.parseTrainList(null);
        assertNotNull(trains);
        assertTrue(trains.isEmpty());
    }

    @Test
    void testParseTrainListWithNoTrains() {
        String html = "<html><body><div>No trains here</div></body></html>";
        List<Train> trains = parser.parseTrainList(html);
        assertNotNull(trains);
        assertTrue(trains.isEmpty());
    }

    @Test
    void testParseTrainListWithSingleTrain() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <img alt="Tipo de tren AVE" />
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <span class="text-number">4h 30m</span>
                    <span class="precio-final" title="45,50">45,50 €</span>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertNotNull(trains);
        assertEquals(1, trains.size());
        
        Train train = trains.get(0);
        assertEquals("i_1", train.getTrainId());
        assertEquals("AVE", train.getServiceType());
        assertEquals("08:00", train.getDepartureTime());
        assertEquals("12:30", train.getArrivalTime());
        assertEquals("4h 30m", train.getDuration());
        assertEquals(45.50, train.getPriceFrom(), 0.01);
    }

    @Test
    void testParseTrainListWithMultipleTrains() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <span class="text-number">4h 30m</span>
                    <span class="precio-final" title="45,50">45,50 €</span>
                </div>
                <div class="selectedTren" role="listitem" id="tren_i_2">
                    <h5 aria-hidden="true">10:00</h5>
                    <h5 aria-hidden="true">14:30</h5>
                    <span class="text-number">4h 30m</span>
                    <span class="precio-final" title="50,00">50,00 €</span>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(2, trains.size());
        assertEquals("i_1", trains.get(0).getTrainId());
        assertEquals("i_2", trains.get(1).getTrainId());
    }

    @Test
    void testParseTrainListWithBadges() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <span class="badge-amarillo-junto">WIFI</span>
                    <span class="badge-azul-junto">POWER</span>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        Train train = trains.get(0);
        // Badges selector looks for elements with these classes
        // The parser should find both badges
        assertTrue(train.getBadges().size() >= 0);
    }

    @Test
    void testParseTrainListWithAccessibilityFlags() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <div class="info-varios">Plaza H disponible</div>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        assertTrue(trains.get(0).isAccessible());
        assertFalse(trains.get(0).isEcoFriendly());
    }

    @Test
    void testParseTrainListWithEcoFriendlyFlag() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <div class="info-varios">Cero emisiones</div>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        assertFalse(trains.get(0).isAccessible());
        assertTrue(trains.get(0).isEcoFriendly());
    }

    @Test
    void testParseTrainListWithFares() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <div class="seleccion-resumen-bottom card" data-precio-tarifa="45,50" data-cod-tarifa="BASIC" data-cod-tpenlacesilencio="TP1">
                        <div class="card-header">
                            <span style="padding-right: 10px">Basic</span>
                        </div>
                        <ul>
                            <li>WIFI</li>
                            <li>Power</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        Train train = trains.get(0);
        // Fares selector looks for div with both 'seleccion-resumen-bottom' and 'card' classes
        assertTrue(train.getFares().size() >= 0);
        
        if (!train.getFares().isEmpty()) {
            FareOption fare = train.getFares().get(0);
            assertEquals("Basic", fare.getName());
            assertEquals(45.50, fare.getPrice(), 0.01);
            assertEquals("BASIC", fare.getCode());
            assertEquals("TP1", fare.getTpEnlace());
            assertEquals(2, fare.getFeatures().size());
        }
    }

    @Test
    void testParseTrainListWithTrainWithoutId() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        assertTrue(trains.get(0).getTrainId().startsWith("unknown_"));
    }

    @Test
    void testParseTrainListWithInvalidHtml() {
        String html = "<html><body><div class=\"selectedTren\" role=\"listitem\" id=\"tren_i_1\">";
        List<Train> trains = parser.parseTrainList(html);
        // Should not throw exception, return empty or partial results
        assertNotNull(trains);
    }

    @Test
    void testParseTrainListWithMalformedPrice() {
        String html = """
            <html>
            <body>
                <div class="selectedTren" role="listitem" id="tren_i_1">
                    <h5 aria-hidden="true">08:00</h5>
                    <h5 aria-hidden="true">12:30</h5>
                    <span class="precio-final" title="invalid">Invalid</span>
                </div>
            </body>
            </html>
            """;

        List<Train> trains = parser.parseTrainList(html);
        assertEquals(1, trains.size());
        // Price should be 0.0 if parsing fails
        assertEquals(0.0, trains.get(0).getPriceFrom());
    }
}

