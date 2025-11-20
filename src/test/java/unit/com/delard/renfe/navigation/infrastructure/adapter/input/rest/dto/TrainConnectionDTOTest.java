/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for TrainConnectionDTO
 */
class TrainConnectionDTOTest
{

    @Test
    void testRecordCreation()
    {
        // Act
        TrainConnectionDTO dto = new TrainConnectionDTO(
                "1 horas 10 minutos",
                "REG.EXP.",
                "AVE");

        // Assert
        assertNotNull(dto);
        assertEquals("1 horas 10 minutos", dto.duration());
        assertEquals("REG.EXP.", dto.firstTrainType());
        assertEquals("AVE", dto.secondTrainType());
    }

    @Test
    void testRecordCreationWithNullValues()
    {
        // Act
        TrainConnectionDTO dto = new TrainConnectionDTO(
                null,
                null,
                null);

        // Assert
        assertNotNull(dto);
        assertNull(dto.duration());
        assertNull(dto.firstTrainType());
        assertNull(dto.secondTrainType());
    }

    @Test
    void testRecordCreationWithEmptyStrings()
    {
        // Act
        TrainConnectionDTO dto = new TrainConnectionDTO(
                "",
                "",
                "");

        // Assert
        assertNotNull(dto);
        assertEquals("", dto.duration());
        assertEquals("", dto.firstTrainType());
        assertEquals("", dto.secondTrainType());
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
            TrainConnectionDTO dto = new TrainConnectionDTO("1 hora", types[0], types[1]);
            assertEquals(types[0], dto.firstTrainType());
            assertEquals(types[1], dto.secondTrainType());
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
            TrainConnectionDTO dto = new TrainConnectionDTO(duration, "AVE", "ALVIA");
            assertEquals(duration, dto.duration());
        }
    }

    @Test
    void testAllFields()
    {
        // Act
        TrainConnectionDTO dto = new TrainConnectionDTO(
                "1 horas 10 minutos",
                "REG.EXP.",
                "AVE");

        // Assert
        assertEquals("1 horas 10 minutos", dto.duration());
        assertEquals("REG.EXP.", dto.firstTrainType());
        assertEquals("AVE", dto.secondTrainType());
    }

    @Test
    void testRecordEquality()
    {
        // Arrange
        TrainConnectionDTO dto1 = new TrainConnectionDTO("1 hora", "AVE", "ALVIA");
        TrainConnectionDTO dto2 = new TrainConnectionDTO("1 hora", "AVE", "ALVIA");

        // Assert - Records implement equals() based on field values
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordInequality()
    {
        // Arrange
        TrainConnectionDTO dto1 = new TrainConnectionDTO("1 hora", "AVE", "ALVIA");
        TrainConnectionDTO dto2 = new TrainConnectionDTO("2 horas", "AVE", "ALVIA");
        TrainConnectionDTO dto3 = new TrainConnectionDTO("1 hora", "REG.EXP.", "ALVIA");
        TrainConnectionDTO dto4 = new TrainConnectionDTO("1 hora", "AVE", "EUROMED");

        // Assert
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, dto4);
    }

    @Test
    void testToString()
    {
        // Arrange
        TrainConnectionDTO dto = new TrainConnectionDTO("1 hora", "AVE", "ALVIA");

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("TrainConnectionDTO"));
        assertTrue(toString.contains("1 hora"));
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("ALVIA"));
    }

    @Test
    void testWithLongDuration()
    {
        // Test with a longer duration string
        String longDuration = "3 horas 45 minutos";
        TrainConnectionDTO dto = new TrainConnectionDTO(longDuration, "AVE", "ALVIA");

        assertEquals(longDuration, dto.duration());
    }

    @Test
    void testWithSpecialCharactersInTrainTypes()
    {
        // Test with train types that contain special characters (dots, spaces)
        TrainConnectionDTO dto = new TrainConnectionDTO(
                "1 hora",
                "REG.EXP.",
                "AVE");

        assertEquals("REG.EXP.", dto.firstTrainType());
        assertEquals("AVE", dto.secondTrainType());
    }
}
