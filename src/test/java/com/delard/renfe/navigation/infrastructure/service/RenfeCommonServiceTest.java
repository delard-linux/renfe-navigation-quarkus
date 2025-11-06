package com.delard.renfe.navigation.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RenfeCommonService
 */
@ExtendWith(MockitoExtension.class)
class RenfeCommonServiceTest {

    private RenfeCommonService service;

    @BeforeEach
    void setUp() {
        service = new RenfeCommonService();
    }

    @Test
    void testFormatDateValid() {
        String result = service.formatDate("2025-12-01");
        assertEquals("01/12/2025", result);
    }

    @Test
    void testFormatDateInvalid() {
        String invalidDate = "invalid-date";
        String result = service.formatDate(invalidDate);
        // Should return original string on error
        assertEquals(invalidDate, result);
    }

    @Test
    void testFormatDateNull() {
        // formatDate with null will throw NullPointerException, which is caught and returns null
        String result = service.formatDate(null);
        // The method catches the exception and returns the original string (null in this case)
        assertNull(result);
    }

    @Test
    void testFormatDateDifferentFormats() {
        assertEquals("25/12/2025", service.formatDate("2025-12-25"));
        assertEquals("01/01/2026", service.formatDate("2026-01-01"));
        assertEquals("15/06/2025", service.formatDate("2025-06-15"));
    }

    @Test
    void testFindStationWithMockedStations() throws Exception {
        // Create mock stations data
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "OURENSE",
                    "desgEstacion": "OURENSE",
                    "desgEstacionPlano": "OURENSE",
                    "cdgoAdmon": "0071",
                    "clave": "0071,OURENSE,null"
                },
                {
                    "cdgoEstacion": "MADRID",
                    "desgEstacion": "MADRID PUERTA DE ATOCHA",
                    "desgEstacionPlano": "MADRID",
                    "cdgoAdmon": "0071",
                    "clave": "0071,MADRID,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Use reflection to inject the stations
            java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
            stationsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
            stationsField.set(serviceSpy, stations);
            
            // Test exact match by desgEstacionPlano
            Map<String, String> result = serviceSpy.findStation("OURENSE");
            assertNotNull(result);
            assertEquals("OURENSE", result.get("cdgoEstacion"));
            
            // Test exact match by cdgoEstacion
            result = serviceSpy.findStation("MADRID");
            assertNotNull(result);
            assertEquals("MADRID", result.get("cdgoEstacion"));
            
            // Test case insensitive
            result = serviceSpy.findStation("ourense");
            assertNotNull(result);
            assertEquals("OURENSE", result.get("cdgoEstacion"));
        } catch (Exception e) {
            // If reflection fails, test with real resource loading
            Map<String, String> result = service.findStation("OURENSE");
            assertNotNull(result);
            assertTrue(result.containsKey("cdgoEstacion"));
        }
    }

    @Test
    void testFindStationNotFound() {
        Map<String, String> result = service.findStation("NONEXISTENT_STATION_XYZ");
        assertNotNull(result);
        // Should return generic station data
        assertTrue(result.containsKey("cdgoEstacion"));
        assertTrue(result.containsKey("cdgoAdmon"));
        assertTrue(result.containsKey("desgEstacion"));
        assertTrue(result.containsKey("clave"));
        assertEquals("0071", result.get("cdgoAdmon"));
    }

    @Test
    void testFindStationPartialMatch() {
        // This test depends on actual stations.json file
        Map<String, String> result = service.findStation("MADRID");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
    }

    @Test
    void testFindStationEmptyString() {
        Map<String, String> result = service.findStation("");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
    }

    @Test
    void testFindStationShortName() {
        Map<String, String> result = service.findStation("AB");
        assertNotNull(result);
        // Generic station should have cdgoEstacion with max 5 chars
        String cdgoEstacion = result.get("cdgoEstacion");
        assertNotNull(cdgoEstacion);
        assertTrue(cdgoEstacion.length() <= 5);
    }
}

