package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FareOptionDTO
 */
class FareOptionDTOTest {

    @Test
    void testRecordCreation() {
        FareOptionDTO dto = new FareOptionDTO(
            "Basic Fare",
            45.50,
            "EUR",
            "BASIC",
            "https://example.com/link",
            null,
            new ArrayList<>()
        );
        assertNotNull(dto);
        assertEquals("Basic Fare", dto.name());
        assertEquals(45.50, dto.price(), 0.01);
        assertEquals("EUR", dto.currency());
        assertEquals("BASIC", dto.code());
        assertEquals("https://example.com/link", dto.tpEnlace());
        assertNotNull(dto.features());
        assertTrue(dto.features().isEmpty());
    }

    @Test
    void testDefaultCurrency() {
        FareOptionDTO dto = new FareOptionDTO(
            "Test",
            10.0,
            null, // null currency should default to EUR
            "CODE",
            "link",
            null,
            new ArrayList<>()
        );
        assertEquals("EUR", dto.currency());
    }

    @Test
    void testNullPrice() {
        FareOptionDTO dto = new FareOptionDTO(
            "Test",
            null,
            "EUR",
            "CODE",
            "link",
            null,
            new ArrayList<>()
        );
        assertNull(dto.price());
    }

    @Test
    void testFeaturesDefensiveCopy() {
        List<String> originalFeatures = new ArrayList<>(Arrays.asList("WIFI", "POWER"));
        FareOptionDTO dto = new FareOptionDTO(
            "Test",
            10.0,
            "EUR",
            "CODE",
            "link",
            null,
            originalFeatures
        );
        
        // Modify original list
        originalFeatures.add("NEW_FEATURE");
        
        // DTO features should not be affected
        assertEquals(2, dto.features().size());
        assertTrue(dto.features().contains("WIFI"));
        assertTrue(dto.features().contains("POWER"));
        assertFalse(dto.features().contains("NEW_FEATURE"));
    }

    @Test
    void testNullFeaturesDefaultsToEmpty() {
        FareOptionDTO dto = new FareOptionDTO(
            "Test",
            10.0,
            "EUR",
            "CODE",
            "link",
            null,
            null
        );
        assertNotNull(dto.features());
        assertTrue(dto.features().isEmpty());
    }

    @Test
    void testAllFields() {
        List<String> features = Arrays.asList("WIFI", "MEAL");
        FareOptionDTO dto = new FareOptionDTO(
            "Premium Fare",
            89.90,
            "GBP",
            "PREMIUM",
            "https://example.com/premium",
            "PLAN123",
            features
        );

        assertEquals("Premium Fare", dto.name());
        assertEquals(89.90, dto.price(), 0.01);
        assertEquals("GBP", dto.currency());
        assertEquals("PREMIUM", dto.code());
        assertEquals("https://example.com/premium", dto.tpEnlace());
        assertEquals("PLAN123", dto.plan());
        assertEquals(2, dto.features().size());
    }

    @Test
    void testConvenienceConstructor() {
        FareOptionDTO dto = new FareOptionDTO(
            "Basic",
            45.50,
            "BASIC",
            "link"
        );
        assertEquals("Basic", dto.name());
        assertEquals(45.50, dto.price(), 0.01);
        assertEquals("EUR", dto.currency()); // Default currency
        assertEquals("BASIC", dto.code());
        assertEquals("link", dto.tpEnlace());
        assertNull(dto.plan());
        assertNotNull(dto.features());
        assertTrue(dto.features().isEmpty());
    }
}
