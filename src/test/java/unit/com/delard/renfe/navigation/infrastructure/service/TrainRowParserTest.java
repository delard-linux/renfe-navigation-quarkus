package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.Train;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainRowParser
 */
@ExtendWith(MockitoExtension.class)
class TrainRowParserTest {

    private TrainRowParser parser;

    @BeforeEach
    void setUp() {
        parser = new TrainRowParser();
    }

    @Test
    @DisplayName("parseTrainRow should extract train ID from id attribute")
    void testExtractTrainId() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals("i_1", train.getTrainId());
    }

    @Test
    @DisplayName("parseTrainRow should use fallback train ID when id attribute is missing")
    void testExtractTrainIdFallback() {
        String html = """
            <div class="selectedTren" role="listitem">
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 5);
        
        assertEquals("unknown_5", train.getTrainId());
    }

    @Test
    @DisplayName("parseTrainRow should extract service type from image alt text")
    void testExtractServiceType() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <img alt="Imagen de Tren. Tipo de tren AVE" />
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals("AVE", train.getServiceType());
    }

    @Test
    @DisplayName("parseTrainRow should extract departure and arrival times")
    void testExtractTimes() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <h5 aria-hidden="true">08:00</h5>
                <h5 aria-hidden="true">12:30</h5>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals("08:00", train.getDepartureTime());
        assertEquals("12:30", train.getArrivalTime());
    }

    @Test
    @DisplayName("parseTrainRow should extract duration")
    void testExtractDuration() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <span class="text-number">4h 30m</span>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals("4h 30m", train.getDuration());
    }

    @Test
    @DisplayName("parseTrainRow should extract price from title attribute")
    void testExtractPrice() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <span class="precio-final" title="Precio desde 45,50">45,50 €</span>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals(45.50, train.getPriceFrom(), 0.01);
    }

    @Test
    @DisplayName("parseTrainRow should extract price without 'Precio desde' prefix")
    void testExtractPriceWithoutPrefix() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <span class="precio-final" title="63,10">63,10 €</span>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals(63.10, train.getPriceFrom(), 0.01);
    }

    @Test
    @DisplayName("parseTrainRow should handle invalid price format gracefully")
    void testExtractPriceInvalidFormat() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <span class="precio-final" title="invalid">invalid</span>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals(0.0, train.getPriceFrom(), 0.01);
    }

    @Test
    @DisplayName("parseTrainRow should extract badges")
    void testExtractBadges() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <span class="badge-amarillo-junto">WIFI</span>
                <span class="badge-azul-junto">POWER</span>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals(2, train.getBadges().size());
        assertTrue(train.getBadges().contains("WIFI"));
        assertTrue(train.getBadges().contains("POWER"));
    }

    @Test
    @DisplayName("parseTrainRow should extract accessibility flag")
    void testExtractAccessibility() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <div class="info-varios">Plaza H disponible</div>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertTrue(train.isAccessible());
        assertFalse(train.isEcoFriendly());
    }

    @Test
    @DisplayName("parseTrainRow should extract eco-friendly flag")
    void testExtractEcoFriendly() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <div class="info-varios">Cero emisiones</div>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertFalse(train.isAccessible());
        assertTrue(train.isEcoFriendly());
    }

    @Test
    @DisplayName("parseTrainRow should extract both accessibility and eco-friendly flags")
    void testExtractBothFlags() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <div class="info-varios">Plaza H disponible Cero emisiones</div>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertTrue(train.isAccessible());
        assertTrue(train.isEcoFriendly());
    }

    @Test
    @DisplayName("parseTrainRow should handle missing elements gracefully")
    void testParseTrainRowWithMissingElements() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertNotNull(train);
        assertEquals("i_1", train.getTrainId());
        assertNull(train.getServiceType());
        assertNull(train.getDepartureTime());
        assertNull(train.getArrivalTime());
        assertNull(train.getDuration());
        assertEquals(0.0, train.getPriceFrom(), 0.01);
        assertTrue(train.getBadges().isEmpty());
        assertFalse(train.isAccessible());
        assertFalse(train.isEcoFriendly());
    }

    @Test
    @DisplayName("parseTrainRow should handle complete train row")
    void testParseCompleteTrainRow() {
        String html = """
            <div class="selectedTren" role="listitem" id="tren_i_1">
                <img alt="Imagen de Tren. Tipo de tren AVE" />
                <h5 aria-hidden="true">08:00</h5>
                <h5 aria-hidden="true">12:30</h5>
                <span class="text-number">4h 30m</span>
                <span class="precio-final" title="Precio desde 45,50">45,50 €</span>
                <span class="badge-amarillo-junto">WIFI</span>
                <div class="info-varios">Plaza H disponible</div>
            </div>
            """;
        Element row = Jsoup.parse(html).selectFirst("div.selectedTren");
        
        Train train = parser.parseTrainRow(row, 0);
        
        assertEquals("i_1", train.getTrainId());
        assertEquals("AVE", train.getServiceType());
        assertEquals("08:00", train.getDepartureTime());
        assertEquals("12:30", train.getArrivalTime());
        assertEquals("4h 30m", train.getDuration());
        assertEquals(45.50, train.getPriceFrom(), 0.01);
        assertEquals(1, train.getBadges().size());
        assertTrue(train.isAccessible());
    }
}

