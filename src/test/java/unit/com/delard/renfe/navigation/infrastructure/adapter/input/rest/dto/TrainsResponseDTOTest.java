/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for TrainsResponseDTO
 */
class TrainsResponseDTOTest
{

    @Test
    void testRecordCreation()
    {
        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                null,
                "1",
                Collections.emptyList(),
                null);
        assertNotNull(dto);
        assertEquals("OURENSE", dto.origin());
        assertEquals("MADRID", dto.destination());
        assertEquals("2025-12-01", dto.dateOut());
        assertNull(dto.dateReturn());
        assertEquals("1", dto.adults());
        assertNotNull(dto.trainsOut());
        assertTrue(dto.trainsOut().isEmpty());
        assertNull(dto.trainsReturn());
    }

    @Test
    void testTrainsOutDefensiveCopy()
    {
        List<TrainDTO> originalTrains = new ArrayList<>();
        originalTrains.add(createTrainDTO("TRAIN1"));

        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                null,
                "1",
                originalTrains,
                null);

        // Modify original list
        originalTrains.add(createTrainDTO("TRAIN2"));

        // DTO trainsOut should not be affected
        assertEquals(1, dto.trainsOut().size());
        assertEquals("TRAIN1", dto.trainsOut().get(0).trainId());
    }

    @Test
    void testNullTrainsOutDefaultsToEmpty()
    {
        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                null,
                "1",
                null,
                null);
        assertNotNull(dto.trainsOut());
        assertTrue(dto.trainsOut().isEmpty());
    }

    @Test
    void testTrainsReturnDefensiveCopy()
    {
        List<TrainDTO> originalTrains = new ArrayList<>();
        originalTrains.add(createTrainDTO("RETURN1"));

        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                "2025-12-05",
                "1",
                Collections.emptyList(),
                originalTrains);

        // Modify original list
        originalTrains.add(createTrainDTO("RETURN2"));

        // DTO trainsReturn should not be affected
        assertEquals(1, dto.trainsReturn().size());
        assertEquals("RETURN1", dto.trainsReturn().get(0).trainId());
    }

    @Test
    void testNullTrainsReturnRemainsNull()
    {
        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                null,
                "1",
                Collections.emptyList(),
                null);
        assertNull(dto.trainsReturn());
    }

    @Test
    void testAllFields()
    {
        List<TrainDTO> trainsOut = Arrays.asList(createTrainDTO("TRAIN1"));
        List<TrainDTO> trainsReturn = Arrays.asList(createTrainDTO("RETURN1"));

        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                "2025-12-05",
                "2",
                trainsOut,
                trainsReturn);

        assertEquals("OURENSE", dto.origin());
        assertEquals("MADRID", dto.destination());
        assertEquals("2025-12-01", dto.dateOut());
        assertEquals("2025-12-05", dto.dateReturn());
        assertEquals("2", dto.adults());
        assertEquals(1, dto.trainsOut().size());
        assertEquals(1, dto.trainsReturn().size());
    }

    @Test
    void testEmptyTrainsOut()
    {
        TrainsResponseDTO dto = new TrainsResponseDTO(
                "OURENSE",
                "MADRID",
                "2025-12-01",
                null,
                "1",
                Collections.emptyList(),
                null);
        assertNotNull(dto.trainsOut());
        assertTrue(dto.trainsOut().isEmpty());
    }

    private TrainDTO createTrainDTO(String trainId)
    {
        return new TrainDTO(
                trainId,
                "AVE",
                "08:00",
                "12:00",
                "4h",
                50.0,
                "EUR",
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                false,
                null);
    }
}
