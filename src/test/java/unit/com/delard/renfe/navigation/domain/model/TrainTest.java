/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.model;


import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for Train domain model
 */
class TrainTest
{

    @Test
    void testDefaultConstructor()
    {
        // Act
        Train train = new Train();

        // Assert
        assertNotNull(train);
        assertNull(train.getTrainId());
        assertNull(train.getServiceType());
        assertNull(train.getDepartureTime());
        assertNull(train.getArrivalTime());
        assertNull(train.getDuration());
        assertEquals(0.0, train.getPriceFrom());
        assertEquals("EUR", train.getCurrency());
        assertNotNull(train.getFares());
        assertTrue(train.getFares().isEmpty());
        assertNotNull(train.getBadges());
        assertTrue(train.getBadges().isEmpty());
        assertFalse(train.isAccessible());
        assertFalse(train.isEcoFriendly());
    }

    @Test
    void testParameterizedConstructor()
    {
        // Arrange
        String trainId = "TRAIN123";
        String serviceType = "AVE";
        String departureTime = "08:00";
        String arrivalTime = "12:30";
        String duration = "4h 30m";
        double priceFrom = 45.50;

        // Act
        Train train = new Train(trainId, serviceType, departureTime, arrivalTime, duration, priceFrom);

        // Assert
        assertEquals(trainId, train.getTrainId());
        assertEquals(serviceType, train.getServiceType());
        assertEquals(departureTime, train.getDepartureTime());
        assertEquals(arrivalTime, train.getArrivalTime());
        assertEquals(duration, train.getDuration());
        assertEquals(priceFrom, train.getPriceFrom());
        assertEquals("EUR", train.getCurrency());
        assertNotNull(train.getFares());
        assertTrue(train.getFares().isEmpty());
        assertNotNull(train.getBadges());
        assertTrue(train.getBadges().isEmpty());
        assertFalse(train.isAccessible());
        assertFalse(train.isEcoFriendly());
    }

    @Test
    void testSettersAndGetters()
    {
        // Arrange
        Train train = new Train();
        String trainId = "TRAIN456";
        String serviceType = "ALVIA";
        String departureTime = "10:00";
        String arrivalTime = "15:30";
        String duration = "5h 30m";
        double priceFrom = 67.80;
        String currency = "USD";
        boolean accessible = true;
        boolean ecoFriendly = true;

        // Act
        train.setTrainId(trainId);
        train.setServiceType(serviceType);
        train.setDepartureTime(departureTime);
        train.setArrivalTime(arrivalTime);
        train.setDuration(duration);
        train.setPriceFrom(priceFrom);
        train.setCurrency(currency);
        train.setAccessible(accessible);
        train.setEcoFriendly(ecoFriendly);

        // Assert
        assertEquals(trainId, train.getTrainId());
        assertEquals(serviceType, train.getServiceType());
        assertEquals(departureTime, train.getDepartureTime());
        assertEquals(arrivalTime, train.getArrivalTime());
        assertEquals(duration, train.getDuration());
        assertEquals(priceFrom, train.getPriceFrom());
        assertEquals(currency, train.getCurrency());
        assertEquals(accessible, train.isAccessible());
        assertEquals(ecoFriendly, train.isEcoFriendly());
    }

    @Test
    void testSetFaresWithNull()
    {
        // Arrange
        Train train = new Train();

        // Act
        train.setFares(null);

        // Assert
        assertNotNull(train.getFares());
        assertTrue(train.getFares().isEmpty());
    }

    @Test
    void testSetFaresWithList()
    {
        // Arrange
        Train train = new Train();
        FareOption fare1 = new FareOption("Basic", 45.50, "EUR", "BASIC", null, null);
        FareOption fare2 = new FareOption("Premium", 89.90, "EUR", "PREMIUM", null, null);
        List<FareOption> fares = Arrays.asList(fare1, fare2);

        // Act
        train.setFares(fares);

        // Assert
        assertNotNull(train.getFares());
        assertEquals(2, train.getFares().size());
        assertEquals(fare1.getName(), train.getFares().get(0).getName());
        assertEquals(fare2.getName(), train.getFares().get(1).getName());
    }

    @Test
    void testSetFaresReturnsNewList()
    {
        // Arrange
        Train train = new Train();
        FareOption fare1 = new FareOption("Basic", 45.50, "EUR", "BASIC", null, null);
        List<FareOption> fares = new ArrayList<>();
        fares.add(fare1);

        // Act
        train.setFares(fares);
        List<FareOption> returnedFares = train.getFares();
        fares.add(new FareOption("Premium", 89.90, "EUR", "PREMIUM", null, null));

        // Assert - modifying original list should not affect returned list
        assertEquals(1, returnedFares.size());
        assertEquals(2, fares.size());
    }

    @Test
    void testGetFaresReturnsNewList()
    {
        // Arrange
        Train train = new Train();
        FareOption fare1 = new FareOption("Basic", 45.50, "EUR", "BASIC", null, null);
        train.setFares(Arrays.asList(fare1));

        // Act
        List<FareOption> fares1 = train.getFares();
        List<FareOption> fares2 = train.getFares();

        // Assert - should return different instances
        assertNotSame(fares1, fares2);
        assertEquals(fares1.size(), fares2.size());
    }

    @Test
    void testSetBadgesWithNull()
    {
        // Arrange
        Train train = new Train();

        // Act
        train.setBadges(null);

        // Assert
        assertNotNull(train.getBadges());
        assertTrue(train.getBadges().isEmpty());
    }

    @Test
    void testSetBadgesWithList()
    {
        // Arrange
        Train train = new Train();
        List<String> badges = Arrays.asList("WIFI", "POWER", "MEAL");

        // Act
        train.setBadges(badges);

        // Assert
        assertNotNull(train.getBadges());
        assertEquals(3, train.getBadges().size());
        assertTrue(train.getBadges().contains("WIFI"));
        assertTrue(train.getBadges().contains("POWER"));
        assertTrue(train.getBadges().contains("MEAL"));
    }

    @Test
    void testSetBadgesReturnsNewList()
    {
        // Arrange
        Train train = new Train();
        List<String> badges = new ArrayList<>();
        badges.add("WIFI");

        // Act
        train.setBadges(badges);
        List<String> returnedBadges = train.getBadges();
        badges.add("POWER");

        // Assert - modifying original list should not affect returned list
        assertEquals(1, returnedBadges.size());
        assertEquals(2, badges.size());
    }

    @Test
    void testGetBadgesReturnsNewList()
    {
        // Arrange
        Train train = new Train();
        train.setBadges(Arrays.asList("WIFI", "POWER"));

        // Act
        List<String> badges1 = train.getBadges();
        List<String> badges2 = train.getBadges();

        // Assert - should return different instances
        assertNotSame(badges1, badges2);
        assertEquals(badges1.size(), badges2.size());
    }

    @Test
    void testSetPriceFromWithZero()
    {
        // Arrange
        Train train = new Train();

        // Act
        train.setPriceFrom(0.0);

        // Assert
        assertEquals(0.0, train.getPriceFrom());
    }

    @Test
    void testSetPriceFromWithNegative()
    {
        // Arrange
        Train train = new Train();

        // Act
        train.setPriceFrom(-10.50);

        // Assert
        assertEquals(-10.50, train.getPriceFrom());
    }

    @Test
    void testSetAccessible()
    {
        // Arrange
        Train train = new Train();

        // Act & Assert
        train.setAccessible(true);
        assertTrue(train.isAccessible());

        train.setAccessible(false);
        assertFalse(train.isAccessible());
    }

    @Test
    void testSetEcoFriendly()
    {
        // Arrange
        Train train = new Train();

        // Act & Assert
        train.setEcoFriendly(true);
        assertTrue(train.isEcoFriendly());

        train.setEcoFriendly(false);
        assertFalse(train.isEcoFriendly());
    }

    @Test
    void testSetNullValues()
    {
        // Arrange
        Train train = new Train();

        // Act
        train.setTrainId(null);
        train.setServiceType(null);
        train.setDepartureTime(null);
        train.setArrivalTime(null);
        train.setDuration(null);
        train.setCurrency(null);

        // Assert
        assertNull(train.getTrainId());
        assertNull(train.getServiceType());
        assertNull(train.getDepartureTime());
        assertNull(train.getArrivalTime());
        assertNull(train.getDuration());
        assertNull(train.getCurrency());
    }
}
