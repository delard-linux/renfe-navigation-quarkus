package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainDTO
 */
class TrainDTOTest {

    @Test
    void testDefaultConstructor() {
        TrainDTO dto = new TrainDTO();
        assertNotNull(dto);
        assertEquals("EUR", dto.getCurrency());
        assertNotNull(dto.getFares());
        assertTrue(dto.getFares().isEmpty());
        assertNotNull(dto.getBadges());
        assertTrue(dto.getBadges().isEmpty());
        assertFalse(dto.isAccessible());
        assertFalse(dto.isEcoFriendly());
    }

    @Test
    void testGettersAndSetters() {
        TrainDTO dto = new TrainDTO();

        // Test trainId
        dto.setTrainId("TRAIN123");
        assertEquals("TRAIN123", dto.getTrainId());

        // Test serviceType
        dto.setServiceType("AVE");
        assertEquals("AVE", dto.getServiceType());

        // Test departureTime
        dto.setDepartureTime("08:00");
        assertEquals("08:00", dto.getDepartureTime());

        // Test arrivalTime
        dto.setArrivalTime("12:30");
        assertEquals("12:30", dto.getArrivalTime());

        // Test duration
        dto.setDuration("4h 30m");
        assertEquals("4h 30m", dto.getDuration());

        // Test priceFrom
        dto.setPriceFrom(45.50);
        assertEquals(45.50, dto.getPriceFrom(), 0.01);

        // Test currency
        dto.setCurrency("USD");
        assertEquals("USD", dto.getCurrency());

        // Test accessible
        dto.setAccessible(true);
        assertTrue(dto.isAccessible());

        // Test ecoFriendly
        dto.setEcoFriendly(true);
        assertTrue(dto.isEcoFriendly());
    }

    @Test
    void testFaresGetterAndSetter() {
        TrainDTO dto = new TrainDTO();
        List<FareOptionDTO> fares = new ArrayList<>();
        FareOptionDTO fare1 = new FareOptionDTO();
        fare1.setName("Basic");
        fare1.setPrice(45.50);
        fares.add(fare1);

        FareOptionDTO fare2 = new FareOptionDTO();
        fare2.setName("Premium");
        fare2.setPrice(89.90);
        fares.add(fare2);

        dto.setFares(fares);
        List<FareOptionDTO> retrievedFares = dto.getFares();
        assertNotNull(retrievedFares);
        assertEquals(2, retrievedFares.size());
        assertEquals("Basic", retrievedFares.get(0).getName());
        assertEquals("Premium", retrievedFares.get(1).getName());

        // Test that getter returns a copy
        retrievedFares.add(new FareOptionDTO());
        assertEquals(2, dto.getFares().size());
    }

    @Test
    void testFaresSetterWithNull() {
        TrainDTO dto = new TrainDTO();
        dto.setFares(null);
        assertNotNull(dto.getFares());
        assertTrue(dto.getFares().isEmpty());
    }

    @Test
    void testBadgesGetterAndSetter() {
        TrainDTO dto = new TrainDTO();
        List<String> badges = Arrays.asList("WIFI", "POWER", "RESTAURANT");

        dto.setBadges(badges);
        List<String> retrievedBadges = dto.getBadges();
        assertNotNull(retrievedBadges);
        assertEquals(3, retrievedBadges.size());
        assertTrue(retrievedBadges.contains("WIFI"));
        assertTrue(retrievedBadges.contains("POWER"));
        assertTrue(retrievedBadges.contains("RESTAURANT"));

        // Test that getter returns a copy
        retrievedBadges.add("NEW_BADGE");
        assertEquals(3, dto.getBadges().size());
    }

    @Test
    void testBadgesSetterWithNull() {
        TrainDTO dto = new TrainDTO();
        dto.setBadges(null);
        assertNotNull(dto.getBadges());
        assertTrue(dto.getBadges().isEmpty());
    }

    @Test
    void testAllFields() {
        TrainDTO dto = new TrainDTO();
        dto.setTrainId("TRAIN456");
        dto.setServiceType("ALVIA");
        dto.setDepartureTime("09:15");
        dto.setArrivalTime("14:45");
        dto.setDuration("5h 30m");
        dto.setPriceFrom(67.80);
        dto.setCurrency("GBP");
        dto.setAccessible(true);
        dto.setEcoFriendly(true);

        List<FareOptionDTO> fares = new ArrayList<>();
        FareOptionDTO fare = new FareOptionDTO();
        fare.setName("Standard");
        fare.setPrice(67.80);
        fares.add(fare);
        dto.setFares(fares);

        List<String> badges = Arrays.asList("WIFI");
        dto.setBadges(badges);

        assertEquals("TRAIN456", dto.getTrainId());
        assertEquals("ALVIA", dto.getServiceType());
        assertEquals("09:15", dto.getDepartureTime());
        assertEquals("14:45", dto.getArrivalTime());
        assertEquals("5h 30m", dto.getDuration());
        assertEquals(67.80, dto.getPriceFrom(), 0.01);
        assertEquals("GBP", dto.getCurrency());
        assertTrue(dto.isAccessible());
        assertTrue(dto.isEcoFriendly());
        assertEquals(1, dto.getFares().size());
        assertEquals(1, dto.getBadges().size());
    }
}

