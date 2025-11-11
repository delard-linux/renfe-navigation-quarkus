package com.delard.renfe.navigation.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Station domain model
 */
class StationTest {

    @Test
    void testStationConstructor() {
        Station station = new Station();
        assertNotNull(station);
    }

    @Test
    void testStationConstructorWithParameters() {
        Station station = new Station(
                "MADRI", "0071", 1, null,
                "MADRID (TODAS)", null,
                "0071,MADRI,null", "MADRID (TODAS)"
        );

        assertEquals("MADRI", station.getStationCode());
        assertEquals("0071", station.getAdministrationCode());
        assertEquals(1, station.getPriority());
        assertNull(station.getDescription());
        assertEquals("MADRID (TODAS)", station.getStationName());
        assertNull(station.getUicCode());
        assertEquals("0071,MADRI,null", station.getKey());
        assertEquals("MADRID (TODAS)", station.getStationNamePlano());
    }

    @Test
    void testStationGettersAndSetters() {
        Station station = new Station();
        
        station.setStationCode("BARCE");
        station.setAdministrationCode("0071");
        station.setPriority(3);
        station.setDescription("Test description");
        station.setStationName("BARCELONA (TODAS)");
        station.setUicCode("71801");
        station.setKey("0071,BARCE,null");
        station.setStationNamePlano("BARCELONA (TODAS)");

        assertEquals("BARCE", station.getStationCode());
        assertEquals("0071", station.getAdministrationCode());
        assertEquals(3, station.getPriority());
        assertEquals("Test description", station.getDescription());
        assertEquals("BARCELONA (TODAS)", station.getStationName());
        assertEquals("71801", station.getUicCode());
        assertEquals("0071,BARCE,null", station.getKey());
        assertEquals("BARCELONA (TODAS)", station.getStationNamePlano());
    }

    @Test
    void testStationEquals() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station station2 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station station3 = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        assertEquals(station1, station2);
        assertNotEquals(station1, station3);
        assertEquals(station1.hashCode(), station2.hashCode());
    }

    @Test
    void testStationEqualsWithNull() {
        Station station = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        
        assertNotEquals(station, null);
        assertNotEquals(station, "not a station");
    }

    @Test
    void testStationToString() {
        Station station = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        
        String toString = station.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("MADRI"));
        assertTrue(toString.contains("MADRID (TODAS)"));
    }
}

