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
    void testDefaultConstructor() {
        FareOptionDTO dto = new FareOptionDTO();
        assertNotNull(dto);
        assertEquals("EUR", dto.getCurrency());
        assertNotNull(dto.getFeatures());
        assertTrue(dto.getFeatures().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        FareOptionDTO dto = new FareOptionDTO();

        // Test name
        dto.setName("Basic Fare");
        assertEquals("Basic Fare", dto.getName());

        // Test price
        dto.setPrice(45.50);
        assertEquals(45.50, dto.getPrice(), 0.01);

        // Test price with null
        dto.setPrice(null);
        assertNull(dto.getPrice());

        // Test currency
        dto.setCurrency("USD");
        assertEquals("USD", dto.getCurrency());

        // Test code
        dto.setCode("BASIC");
        assertEquals("BASIC", dto.getCode());

        // Test tpEnlace
        dto.setTpEnlace("https://example.com/link");
        assertEquals("https://example.com/link", dto.getTpEnlace());
    }

    @Test
    void testFeaturesGetterAndSetter() {
        FareOptionDTO dto = new FareOptionDTO();
        List<String> features = Arrays.asList("WIFI", "POWER", "SEAT_SELECTION");

        dto.setFeatures(features);
        List<String> retrievedFeatures = dto.getFeatures();
        assertNotNull(retrievedFeatures);
        assertEquals(3, retrievedFeatures.size());
        assertTrue(retrievedFeatures.contains("WIFI"));
        assertTrue(retrievedFeatures.contains("POWER"));
        assertTrue(retrievedFeatures.contains("SEAT_SELECTION"));

        // Test that getter returns a copy
        retrievedFeatures.add("NEW_FEATURE");
        assertEquals(3, dto.getFeatures().size());
    }

    @Test
    void testFeaturesSetterWithNull() {
        FareOptionDTO dto = new FareOptionDTO();
        dto.setFeatures(null);
        assertNotNull(dto.getFeatures());
        assertTrue(dto.getFeatures().isEmpty());
    }

    @Test
    void testAllFields() {
        FareOptionDTO dto = new FareOptionDTO();
        dto.setName("Premium Fare");
        dto.setPrice(89.90);
        dto.setCurrency("GBP");
        dto.setCode("PREMIUM");
        dto.setTpEnlace("https://example.com/premium");
        List<String> features = Arrays.asList("WIFI", "MEAL");
        dto.setFeatures(features);

        assertEquals("Premium Fare", dto.getName());
        assertEquals(89.90, dto.getPrice(), 0.01);
        assertEquals("GBP", dto.getCurrency());
        assertEquals("PREMIUM", dto.getCode());
        assertEquals("https://example.com/premium", dto.getTpEnlace());
        assertEquals(2, dto.getFeatures().size());
    }

    @Test
    void testEmptyFeatures() {
        FareOptionDTO dto = new FareOptionDTO();
        dto.setFeatures(new ArrayList<>());
        assertNotNull(dto.getFeatures());
        assertTrue(dto.getFeatures().isEmpty());
    }
}

