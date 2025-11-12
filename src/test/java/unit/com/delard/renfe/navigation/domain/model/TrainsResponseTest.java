package com.delard.renfe.navigation.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainsResponse domain model
 */
class TrainsResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        TrainsResponse response = new TrainsResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getOrigin());
        assertNull(response.getDestination());
        assertNull(response.getDateOut());
        assertNull(response.getDateReturn());
        assertNull(response.getAdults());
        assertNull(response.getTrainsOut());
        assertNull(response.getTrainsReturn());
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        String adults = "2";
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train train2 = new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80);
        List<Train> trainsOut = Arrays.asList(train1, train2);
        List<Train> trainsReturn = Arrays.asList(train1);

        // Act
        TrainsResponse response = new TrainsResponse(origin, destination, dateOut, dateReturn, adults, trainsOut, trainsReturn);

        // Assert
        assertEquals(origin, response.getOrigin());
        assertEquals(destination, response.getDestination());
        assertEquals(dateOut, response.getDateOut());
        assertEquals(dateReturn, response.getDateReturn());
        assertEquals(adults, response.getAdults());
        assertNotNull(response.getTrainsOut());
        assertEquals(2, response.getTrainsOut().size());
        assertNotNull(response.getTrainsReturn());
        assertEquals(1, response.getTrainsReturn().size());
    }

    @Test
    void testParameterizedConstructorWithNullLists() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "1";
        List<Train> trainsOut = null;
        List<Train> trainsReturn = null;

        // Act
        TrainsResponse response = new TrainsResponse(origin, destination, dateOut, dateReturn, adults, trainsOut, trainsReturn);

        // Assert
        assertEquals(origin, response.getOrigin());
        assertEquals(destination, response.getDestination());
        assertEquals(dateOut, response.getDateOut());
        assertNull(response.getDateReturn());
        assertEquals(adults, response.getAdults());
        assertNull(response.getTrainsOut());
        assertNull(response.getTrainsReturn());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        String origin = "BARCELONA";
        String destination = "VALENCIA";
        String dateOut = "2025-12-10";
        String dateReturn = "2025-12-15";
        String adults = "3";

        // Act
        response.setOrigin(origin);
        response.setDestination(destination);
        response.setDateOut(dateOut);
        response.setDateReturn(dateReturn);
        response.setAdults(adults);

        // Assert
        assertEquals(origin, response.getOrigin());
        assertEquals(destination, response.getDestination());
        assertEquals(dateOut, response.getDateOut());
        assertEquals(dateReturn, response.getDateReturn());
        assertEquals(adults, response.getAdults());
    }

    @Test
    void testSetTrainsOutWithNull() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setTrainsOut(null);

        // Assert
        assertNull(response.getTrainsOut());
    }

    @Test
    void testSetTrainsOutWithList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train train2 = new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80);
        List<Train> trains = Arrays.asList(train1, train2);

        // Act
        response.setTrainsOut(trains);

        // Assert
        assertNotNull(response.getTrainsOut());
        assertEquals(2, response.getTrainsOut().size());
        assertEquals("TRAIN123", response.getTrainsOut().get(0).getTrainId());
        assertEquals("TRAIN456", response.getTrainsOut().get(1).getTrainId());
    }

    @Test
    void testSetTrainsOutReturnsNewList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trains = new ArrayList<>();
        trains.add(train1);

        // Act
        response.setTrainsOut(trains);
        List<Train> returnedTrains = response.getTrainsOut();
        trains.add(new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80));

        // Assert - modifying original list should not affect returned list
        assertEquals(1, returnedTrains.size());
        assertEquals(2, trains.size());
    }

    @Test
    void testGetTrainsOutReturnsNewList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        response.setTrainsOut(Arrays.asList(train1));

        // Act
        List<Train> trains1 = response.getTrainsOut();
        List<Train> trains2 = response.getTrainsOut();

        // Assert - should return different instances
        assertNotSame(trains1, trains2);
        assertEquals(trains1.size(), trains2.size());
    }

    @Test
    void testSetTrainsReturnWithNull() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setTrainsReturn(null);

        // Assert
        assertNull(response.getTrainsReturn());
    }

    @Test
    void testSetTrainsReturnWithList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trains = Arrays.asList(train1);

        // Act
        response.setTrainsReturn(trains);

        // Assert
        assertNotNull(response.getTrainsReturn());
        assertEquals(1, response.getTrainsReturn().size());
        assertEquals("TRAIN123", response.getTrainsReturn().get(0).getTrainId());
    }

    @Test
    void testSetTrainsReturnReturnsNewList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trains = new ArrayList<>();
        trains.add(train1);

        // Act
        response.setTrainsReturn(trains);
        List<Train> returnedTrains = response.getTrainsReturn();
        trains.add(new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80));

        // Assert - modifying original list should not affect returned list
        assertEquals(1, returnedTrains.size());
        assertEquals(2, trains.size());
    }

    @Test
    void testGetTrainsReturnReturnsNewList() {
        // Arrange
        TrainsResponse response = new TrainsResponse();
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        response.setTrainsReturn(Arrays.asList(train1));

        // Act
        List<Train> trains1 = response.getTrainsReturn();
        List<Train> trains2 = response.getTrainsReturn();

        // Assert - should return different instances
        assertNotSame(trains1, trains2);
        assertEquals(trains1.size(), trains2.size());
    }

    @Test
    void testSetAdultsWithZero() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setAdults("0");

        // Assert
        assertEquals("0", response.getAdults());
    }

    @Test
    void testSetAdultsWithNegative() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setAdults("-1");

        // Assert
        assertEquals("-1", response.getAdults());
    }

    @Test
    void testSetNullValues() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setOrigin(null);
        response.setDestination(null);
        response.setDateOut(null);
        response.setDateReturn(null);

        // Assert
        assertNull(response.getOrigin());
        assertNull(response.getDestination());
        assertNull(response.getDateOut());
        assertNull(response.getDateReturn());
    }

    @Test
    void testSetEmptyLists() {
        // Arrange
        TrainsResponse response = new TrainsResponse();

        // Act
        response.setTrainsOut(new ArrayList<>());
        response.setTrainsReturn(new ArrayList<>());

        // Assert
        assertNotNull(response.getTrainsOut());
        assertTrue(response.getTrainsOut().isEmpty());
        assertNotNull(response.getTrainsReturn());
        assertTrue(response.getTrainsReturn().isEmpty());
    }
}

