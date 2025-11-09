package com.delard.renfe.navigation.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FareOption domain model
 */
class FareOptionTest {

    @Test
    void testDefaultConstructor() {
        // Act
        FareOption fareOption = new FareOption();

        // Assert
        assertNotNull(fareOption);
        assertNull(fareOption.getName());
        assertEquals(0.0, fareOption.getPrice());
        assertEquals("EUR", fareOption.getCurrency());
        assertNull(fareOption.getCode());
        assertNull(fareOption.getTpEnlace());
        assertNull(fareOption.getPlan());
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithAllParameters() {
        // Arrange
        String name = "Basic";
        double price = 45.50;
        String currency = "EUR";
        String code = "BASIC";
        String tpEnlace = "https://example.com/basic";
        List<String> features = Arrays.asList("WIFI", "POWER");

        // Act
        FareOption fareOption = new FareOption(name, price, currency, code, tpEnlace, features);

        // Assert
        assertEquals(name, fareOption.getName());
        assertEquals(price, fareOption.getPrice());
        assertEquals(currency, fareOption.getCurrency());
        assertEquals(code, fareOption.getCode());
        assertEquals(tpEnlace, fareOption.getTpEnlace());
        assertNotNull(fareOption.getFeatures());
        assertEquals(2, fareOption.getFeatures().size());
        assertTrue(fareOption.getFeatures().contains("WIFI"));
        assertTrue(fareOption.getFeatures().contains("POWER"));
    }

    @Test
    void testParameterizedConstructorWithNullCurrency() {
        // Arrange
        String name = "Basic";
        double price = 45.50;
        String currency = null;
        String code = "BASIC";
        String tpEnlace = null;
        List<String> features = null;

        // Act
        FareOption fareOption = new FareOption(name, price, currency, code, tpEnlace, features);

        // Assert
        assertEquals(name, fareOption.getName());
        assertEquals(price, fareOption.getPrice());
        assertEquals("EUR", fareOption.getCurrency()); // Should default to EUR
        assertEquals(code, fareOption.getCode());
        assertNull(fareOption.getTpEnlace());
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithNullFeatures() {
        // Arrange
        String name = "Basic";
        double price = 45.50;
        String currency = "USD";
        String code = "BASIC";
        String tpEnlace = "https://example.com";
        List<String> features = null;

        // Act
        FareOption fareOption = new FareOption(name, price, currency, code, tpEnlace, features);

        // Assert
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        FareOption fareOption = new FareOption();
        String name = "Premium";
        double price = 89.90;
        String currency = "USD";
        String code = "PREMIUM";
        String tpEnlace = "https://example.com/premium";
        String plan = "PLAN123";
        List<String> features = Arrays.asList("WIFI", "MEAL");

        // Act
        fareOption.setName(name);
        fareOption.setPrice(price);
        fareOption.setCurrency(currency);
        fareOption.setCode(code);
        fareOption.setTpEnlace(tpEnlace);
        fareOption.setPlan(plan);
        fareOption.setFeatures(features);

        // Assert
        assertEquals(name, fareOption.getName());
        assertEquals(price, fareOption.getPrice());
        assertEquals(currency, fareOption.getCurrency());
        assertEquals(code, fareOption.getCode());
        assertEquals(tpEnlace, fareOption.getTpEnlace());
        assertEquals(plan, fareOption.getPlan());
        assertNotNull(fareOption.getFeatures());
        assertEquals(2, fareOption.getFeatures().size());
    }

    @Test
    void testSetFeaturesWithNull() {
        // Arrange
        FareOption fareOption = new FareOption();

        // Act
        fareOption.setFeatures(null);

        // Assert
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }

    @Test
    void testSetFeaturesWithList() {
        // Arrange
        FareOption fareOption = new FareOption();
        List<String> features = Arrays.asList("WIFI", "POWER", "MEAL");

        // Act
        fareOption.setFeatures(features);

        // Assert
        assertNotNull(fareOption.getFeatures());
        assertEquals(3, fareOption.getFeatures().size());
        assertTrue(fareOption.getFeatures().contains("WIFI"));
        assertTrue(fareOption.getFeatures().contains("POWER"));
        assertTrue(fareOption.getFeatures().contains("MEAL"));
    }

    @Test
    void testSetFeaturesReturnsNewList() {
        // Arrange
        FareOption fareOption = new FareOption();
        List<String> features = new ArrayList<>();
        features.add("WIFI");

        // Act
        fareOption.setFeatures(features);
        List<String> returnedFeatures = fareOption.getFeatures();
        features.add("POWER");

        // Assert - modifying original list should not affect returned list
        assertEquals(1, returnedFeatures.size());
        assertEquals(2, features.size());
    }

    @Test
    void testGetFeaturesReturnsNewList() {
        // Arrange
        FareOption fareOption = new FareOption();
        fareOption.setFeatures(Arrays.asList("WIFI", "POWER"));

        // Act
        List<String> features1 = fareOption.getFeatures();
        List<String> features2 = fareOption.getFeatures();

        // Assert - should return different instances
        assertNotSame(features1, features2);
        assertEquals(features1.size(), features2.size());
    }

    @Test
    void testSetPriceWithZero() {
        // Arrange
        FareOption fareOption = new FareOption();

        // Act
        fareOption.setPrice(0.0);

        // Assert
        assertEquals(0.0, fareOption.getPrice());
    }

    @Test
    void testSetPriceWithNegative() {
        // Arrange
        FareOption fareOption = new FareOption();

        // Act
        fareOption.setPrice(-10.50);

        // Assert
        assertEquals(-10.50, fareOption.getPrice());
    }

    @Test
    void testSetNullValues() {
        // Arrange
        FareOption fareOption = new FareOption();

        // Act
        fareOption.setName(null);
        fareOption.setCurrency(null);
        fareOption.setCode(null);
        fareOption.setTpEnlace(null);
        fareOption.setPlan(null);

        // Assert
        assertNull(fareOption.getName());
        assertNull(fareOption.getCurrency());
        assertNull(fareOption.getCode());
        assertNull(fareOption.getTpEnlace());
        assertNull(fareOption.getPlan());
    }

    @Test
    void testSetEmptyFeatures() {
        // Arrange
        FareOption fareOption = new FareOption();

        // Act
        fareOption.setFeatures(new ArrayList<>());

        // Assert
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithEmptyFeatures() {
        // Arrange
        String name = "Basic";
        double price = 45.50;
        String currency = "EUR";
        String code = "BASIC";
        String tpEnlace = null;
        List<String> features = new ArrayList<>();

        // Act
        FareOption fareOption = new FareOption(name, price, currency, code, tpEnlace, features);

        // Assert
        assertNotNull(fareOption.getFeatures());
        assertTrue(fareOption.getFeatures().isEmpty());
    }
}

