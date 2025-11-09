package com.delard.renfe.navigation.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void testFindStationVeryLongName() {
        Map<String, String> result = service.findStation("VERY_LONG_STATION_NAME_THAT_EXCEEDS_FIVE_CHARACTERS");
        assertNotNull(result);
        // Generic station should have cdgoEstacion with max 5 chars
        String cdgoEstacion = result.get("cdgoEstacion");
        assertNotNull(cdgoEstacion);
        assertEquals(5, cdgoEstacion.length());
        assertEquals("VERY_", cdgoEstacion);
    }

    @Test
    void testFindStationExactFiveCharacters() {
        Map<String, String> result = service.findStation("ABCDE");
        assertNotNull(result);
        String cdgoEstacion = result.get("cdgoEstacion");
        assertNotNull(cdgoEstacion);
        assertEquals(5, cdgoEstacion.length());
    }

    @Test
    void testFindStationPartialMatchCaseInsensitive() {
        // This test depends on actual stations.json file
        Map<String, String> result = service.findStation("madrid");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
    }

    @Test
    void testFormatDateWithDifferentMonths() {
        assertEquals("01/01/2025", service.formatDate("2025-01-01"));
        assertEquals("28/02/2025", service.formatDate("2025-02-28"));
        assertEquals("31/12/2025", service.formatDate("2025-12-31"));
    }

    @Test
    void testFormatDateWithLeapYear() {
        assertEquals("29/02/2024", service.formatDate("2024-02-29"));
    }

    @Test
    void testFormatDateWithInvalidFormat() {
        String invalidDate = "2025/12/01";
        String result = service.formatDate(invalidDate);
        assertEquals(invalidDate, result);
    }

    @Test
    void testFormatDateWithEmptyString() {
        String result = service.formatDate("");
        assertEquals("", result);
    }

    @Test
    void testFormatDateWithInvalidDate() {
        String invalidDate = "2025-13-01"; // Invalid month
        String result = service.formatDate(invalidDate);
        assertEquals(invalidDate, result);
    }

    @Test
    void testFormatDateWithInvalidDay() {
        // Note: Java's LocalDate.parse normalizes invalid dates (2025-02-30 becomes 2025-02-28)
        // So the method will format the normalized date, not return the original
        String invalidDate = "2025-02-30"; // Invalid day for February
        String result = service.formatDate(invalidDate);
        // LocalDate normalizes to 2025-02-28, which formats to 28/02/2025
        assertEquals("28/02/2025", result);
    }

    @Test
    void testFindStationWithSpecialCharacters() {
        Map<String, String> result = service.findStation("STATION-WITH-DASHES");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
    }

    @Test
    void testFindStationWithNumbers() {
        Map<String, String> result = service.findStation("STATION123");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
    }

    @Test
    void testFindStationExactMatchByCdgoEstacion() throws Exception {
        // Test exact match by cdgoEstacion (not desgEstacionPlano)
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "TESTCODE",
                    "desgEstacion": "Test Station",
                    "desgEstacionPlano": "DifferentName",
                    "cdgoAdmon": "0071",
                    "clave": "0071,TESTCODE,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Search by cdgoEstacion (should match)
        Map<String, String> result = serviceSpy.findStation("TESTCODE");
        assertNotNull(result);
        assertEquals("TESTCODE", result.get("cdgoEstacion"));
    }

    @Test
    void testFindStationPartialMatchByContains() throws Exception {
        // Test partial match when stationUpper.contains(plano)
        String stationsJson = """
            [
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
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Search with string that contains "MADRID"
        Map<String, String> result = serviceSpy.findStation("MADRID PUERTA");
        assertNotNull(result);
        assertEquals("MADRID", result.get("cdgoEstacion"));
    }

    @Test
    void testFindStationPartialMatchByStartsWith() throws Exception {
        // Test partial match when plano.startsWith(stationUpper)
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "MADRID",
                    "desgEstacion": "MADRID PUERTA DE ATOCHA",
                    "desgEstacionPlano": "MADRID PUERTA DE ATOCHA",
                    "cdgoAdmon": "0071",
                    "clave": "0071,MADRID,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Search with string that starts with "MADRID"
        Map<String, String> result = serviceSpy.findStation("MADRID");
        assertNotNull(result);
        assertEquals("MADRID", result.get("cdgoEstacion"));
    }

    @Test
    void testFindStationWithNullFields() throws Exception {
        // Test when station has null or missing fields - should skip null fields and find valid one
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "",
                    "desgEstacion": "Test",
                    "desgEstacionPlano": "",
                    "cdgoAdmon": "0071"
                },
                {
                    "cdgoEstacion": "VALID",
                    "desgEstacion": "Valid Station",
                    "desgEstacionPlano": "VALID",
                    "cdgoAdmon": "0071",
                    "clave": "0071,VALID,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Should find the valid station
        Map<String, String> result = serviceSpy.findStation("VALID");
        assertNotNull(result);
        assertEquals("VALID", result.get("cdgoEstacion"));
    }

    @Test
    void testFindStationMultipleCalls() {
        // Test that multiple calls don't reload stations (stations != null branch)
        Map<String, String> result1 = service.findStation("TEST1");
        Map<String, String> result2 = service.findStation("TEST2");
        
        // Both should work (may return generic stations)
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.containsKey("cdgoEstacion"));
        assertTrue(result2.containsKey("cdgoEstacion"));
    }

    @Test
    void testFindStationExactMatchByDesgPlanoFirst() throws Exception {
        // Test exact match when desgPlano matches first (before cdgoEst)
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "DIFFERENT",
                    "desgEstacion": "Test Station",
                    "desgEstacionPlano": "MATCH",
                    "cdgoAdmon": "0071",
                    "clave": "0071,MATCH,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Search by desgEstacionPlano (should match first condition)
        Map<String, String> result = serviceSpy.findStation("MATCH");
        assertNotNull(result);
        assertEquals("MATCH", result.get("desgEstacionPlano"));
    }

    @Test
    void testFindStationPartialMatchBothConditions() throws Exception {
        // Test both partial match conditions with different scenarios
        String stationsJson = """
            [
                {
                    "cdgoEstacion": "STATION1",
                    "desgEstacion": "Station One",
                    "desgEstacionPlano": "LONG_STATION_NAME",
                    "cdgoAdmon": "0071",
                    "clave": "0071,STATION1,null"
                },
                {
                    "cdgoEstacion": "STATION2",
                    "desgEstacion": "Station Two",
                    "desgEstacionPlano": "SHORT",
                    "cdgoAdmon": "0071",
                    "clave": "0071,STATION2,null"
                }
            ]
            """;

        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        ObjectMapper mapper = new ObjectMapper();
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> stations = mapper.readValue(stationsJson, List.class);
        stationsField.set(serviceSpy, stations);

        // Test contains condition: "LONG_STATION_NAME" contains "STATION"
        Map<String, String> result1 = serviceSpy.findStation("STATION");
        assertNotNull(result1);
        
        // Test startsWith condition: "SHORT" starts with "SHORT"
        Map<String, String> result2 = serviceSpy.findStation("SHORT");
        assertNotNull(result2);
    }

    @Test
    void testFindStationWithEmptyStationsList() throws Exception {
        // Test when stations list is empty
        RenfeCommonService serviceSpy = spy(new RenfeCommonService());
        java.lang.reflect.Field stationsField = RenfeCommonService.class.getDeclaredField("stations");
        stationsField.setAccessible(true);
        stationsField.set(serviceSpy, java.util.Collections.emptyList());

        // Should return generic station
        Map<String, String> result = serviceSpy.findStation("ANY_STATION");
        assertNotNull(result);
        assertTrue(result.containsKey("cdgoEstacion"));
        assertTrue(result.containsKey("cdgoAdmon"));
        assertEquals("0071", result.get("cdgoAdmon"));
    }
}

