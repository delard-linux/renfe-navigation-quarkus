/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.model;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for TrainConnection domain model
 */
class TrainConnectionTest
{

    @Test
    void testDefaultConstructor()
    {
        // Act
        TrainConnection connection = new TrainConnection();

        // Assert
        assertNotNull(connection);
        assertNull(connection.getDuration());
        assertNull(connection.getFirstTrainType());
        assertNull(connection.getSecondTrainType());
    }

    @Test
    void testParameterizedConstructor()
    {
        // Arrange
        String duration = "1 horas 10 minutos";
        String firstTrainType = "REG.EXP.";
        String secondTrainType = "AVE";

        // Act
        TrainConnection connection = new TrainConnection(duration, firstTrainType, secondTrainType);

        // Assert
        assertEquals(duration, connection.getDuration());
        assertEquals(firstTrainType, connection.getFirstTrainType());
        assertEquals(secondTrainType, connection.getSecondTrainType());
    }

    @Test
    void testParameterizedConstructorWithNullValues()
    {
        // Arrange
        String duration = null;
        String firstTrainType = null;
        String secondTrainType = null;

        // Act
        TrainConnection connection = new TrainConnection(duration, firstTrainType, secondTrainType);

        // Assert
        assertNull(connection.getDuration());
        assertNull(connection.getFirstTrainType());
        assertNull(connection.getSecondTrainType());
    }

    @Test
    void testSettersAndGetters()
    {
        // Arrange
        TrainConnection connection = new TrainConnection();
        String duration = "45 minutos";
        String firstTrainType = "ALVIA";
        String secondTrainType = "EUROMED";

        // Act
        connection.setDuration(duration);
        connection.setFirstTrainType(firstTrainType);
        connection.setSecondTrainType(secondTrainType);

        // Assert
        assertEquals(duration, connection.getDuration());
        assertEquals(firstTrainType, connection.getFirstTrainType());
        assertEquals(secondTrainType, connection.getSecondTrainType());
    }

    @Test
    void testSetNullValues()
    {
        // Arrange
        TrainConnection connection = new TrainConnection("1 hora", "AVE", "ALVIA");

        // Act
        connection.setDuration(null);
        connection.setFirstTrainType(null);
        connection.setSecondTrainType(null);

        // Assert
        assertNull(connection.getDuration());
        assertNull(connection.getFirstTrainType());
        assertNull(connection.getSecondTrainType());
    }

    @Test
    void testSetEmptyStrings()
    {
        // Arrange
        TrainConnection connection = new TrainConnection();

        // Act
        connection.setDuration("");
        connection.setFirstTrainType("");
        connection.setSecondTrainType("");

        // Assert
        assertEquals("", connection.getDuration());
        assertEquals("", connection.getFirstTrainType());
        assertEquals("", connection.getSecondTrainType());
    }

    @Test
    void testDifferentTrainTypes()
    {
        // Test with various train type combinations
        String[][] trainTypes = {
                { "REG.EXP.", "AVE" },
                { "ALVIA", "EUROMED" },
                { "AVE", "ALVIA" },
                { "EUROMED", "REG.EXP." }
        };

        for (String[] types : trainTypes) {
            TrainConnection connection = new TrainConnection("1 hora", types[0], types[1]);
            assertEquals(types[0], connection.getFirstTrainType());
            assertEquals(types[1], connection.getSecondTrainType());
        }
    }

    @Test
    void testDifferentDurations()
    {
        // Test with various duration formats
        String[] durations = {
                "1 horas 10 minutos",
                "45 minutos",
                "2 horas",
                "30 minutos",
                "1 hora 5 minutos"
        };

        for (String duration : durations) {
            TrainConnection connection = new TrainConnection(duration, "AVE", "ALVIA");
            assertEquals(duration, connection.getDuration());
        }
    }

    @Test
    void testUpdateValues()
    {
        // Arrange
        TrainConnection connection = new TrainConnection("1 hora", "AVE", "ALVIA");

        // Act - Update all values
        connection.setDuration("2 horas");
        connection.setFirstTrainType("REG.EXP.");
        connection.setSecondTrainType("EUROMED");

        // Assert
        assertEquals("2 horas", connection.getDuration());
        assertEquals("REG.EXP.", connection.getFirstTrainType());
        assertEquals("EUROMED", connection.getSecondTrainType());
    }

    @Test
    void testConstructorAndSettersCombination()
    {
        // Arrange
        TrainConnection connection = new TrainConnection("1 hora", "AVE", "ALVIA");

        // Act - Update using setters
        connection.setDuration("45 minutos");
        connection.setFirstTrainType("REG.EXP.");
        connection.setSecondTrainType("EUROMED");

        // Assert
        assertEquals("45 minutos", connection.getDuration());
        assertEquals("REG.EXP.", connection.getFirstTrainType());
        assertEquals("EUROMED", connection.getSecondTrainType());
    }
}
