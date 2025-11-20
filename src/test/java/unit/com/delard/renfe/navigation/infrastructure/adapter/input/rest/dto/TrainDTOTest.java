/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for TrainDTO
 */
class TrainDTOTest
{

    @Test
    void testRecordCreation()
    {
        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                "EUR",
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                false,
                null);
        assertNotNull(dto);
        assertEquals("TRAIN123", dto.trainId());
        assertEquals("AVE", dto.serviceType());
        assertEquals("08:00", dto.departureTime());
        assertEquals("12:30", dto.arrivalTime());
        assertEquals("4h 30m", dto.duration());
        assertEquals(45.50, dto.priceFrom(), 0.01);
        assertEquals("EUR", dto.currency());
        assertNotNull(dto.fares());
        assertTrue(dto.fares().isEmpty());
        assertNotNull(dto.badges());
        assertTrue(dto.badges().isEmpty());
        assertFalse(dto.accessible());
        assertFalse(dto.ecoFriendly());
    }

    @Test
    void testDefaultCurrency()
    {
        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                null, // null currency should default to EUR
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                false,
                null);
        assertEquals("EUR", dto.currency());
    }

    @Test
    void testFaresDefensiveCopy()
    {
        List<FareOptionDTO> originalFares = new ArrayList<>();
        originalFares.add(new FareOptionDTO("Basic", 45.50, "EUR", "BASIC", "link1", null, new ArrayList<>()));

        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                "EUR",
                originalFares,
                new ArrayList<>(),
                false,
                false,
                null);

        // Modify original list
        originalFares.add(new FareOptionDTO("Premium", 89.90, "EUR", "PREMIUM", "link2", null, new ArrayList<>()));

        // DTO fares should not be affected
        assertEquals(1, dto.fares().size());
        assertEquals("Basic", dto.fares().get(0).name());
    }

    @Test
    void testNullFaresDefaultsToEmpty()
    {
        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                "EUR",
                null,
                new ArrayList<>(),
                false,
                false,
                null);
        assertNotNull(dto.fares());
        assertTrue(dto.fares().isEmpty());
    }

    @Test
    void testBadgesDefensiveCopy()
    {
        List<String> originalBadges = new ArrayList<>(Arrays.asList("WIFI", "POWER"));

        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                "EUR",
                new ArrayList<>(),
                originalBadges,
                false,
                false,
                null);

        // Modify original list
        originalBadges.add("NEW_BADGE");

        // DTO badges should not be affected (defensive copy in compact constructor)
        assertEquals(2, dto.badges().size());
        assertTrue(dto.badges().contains("WIFI"));
        assertTrue(dto.badges().contains("POWER"));
        assertFalse(dto.badges().contains("NEW_BADGE"));
    }

    @Test
    void testNullBadgesDefaultsToEmpty()
    {
        TrainDTO dto = new TrainDTO(
                "TRAIN123",
                "AVE",
                "08:00",
                "12:30",
                "4h 30m",
                45.50,
                "EUR",
                new ArrayList<>(),
                null,
                false,
                false,
                null);
        assertNotNull(dto.badges());
        assertTrue(dto.badges().isEmpty());
    }

    @Test
    void testAllFields()
    {
        List<FareOptionDTO> fares = new ArrayList<>();
        fares.add(new FareOptionDTO("Standard", 67.80, "GBP", "STD", "link", null, new ArrayList<>()));

        List<String> badges = Arrays.asList("WIFI");

        TrainDTO dto = new TrainDTO(
                "TRAIN456",
                "ALVIA",
                "09:15",
                "14:45",
                "5h 30m",
                67.80,
                "GBP",
                fares,
                badges,
                true,
                true,
                null);

        assertEquals("TRAIN456", dto.trainId());
        assertEquals("ALVIA", dto.serviceType());
        assertEquals("09:15", dto.departureTime());
        assertEquals("14:45", dto.arrivalTime());
        assertEquals("5h 30m", dto.duration());
        assertEquals(67.80, dto.priceFrom(), 0.01);
        assertEquals("GBP", dto.currency());
        assertTrue(dto.accessible());
        assertTrue(dto.ecoFriendly());
        assertEquals(1, dto.fares().size());
        assertEquals(1, dto.badges().size());
    }
}
