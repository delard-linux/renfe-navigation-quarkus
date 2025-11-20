/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for StationDTO
 */
class StationDTOTest
{

    @Test
    void testRecordCreation()
    {
        StationDTO dto = new StationDTO(
                "MADRI",
                "0071",
                1,
                "Madrid description",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");
        assertNotNull(dto);
        assertEquals("MADRI", dto.stationCode());
        assertEquals("0071", dto.administrationCode());
        assertEquals(1, dto.priority());
        assertEquals("Madrid description", dto.description());
        assertEquals("MADRID (TODAS)", dto.stationName());
        assertEquals("71801", dto.uicCode());
        assertEquals("0071,MADRI,null", dto.key());
        assertEquals("MADRID (TODAS)", dto.stationNamePlano());
    }

    @Test
    void testRecordCreationWithNullValues()
    {
        StationDTO dto = new StationDTO(
                "BARCE",
                "0071",
                null,
                null,
                "BARCELONA (TODAS)",
                null,
                "0071,BARCE,null",
                "BARCELONA (TODAS)");
        assertNotNull(dto);
        assertEquals("BARCE", dto.stationCode());
        assertEquals("0071", dto.administrationCode());
        assertNull(dto.priority());
        assertNull(dto.description());
        assertEquals("BARCELONA (TODAS)", dto.stationName());
        assertNull(dto.uicCode());
        assertEquals("0071,BARCE,null", dto.key());
        assertEquals("BARCELONA (TODAS)", dto.stationNamePlano());
    }

    @Test
    void testRecordCreationWithAllFields()
    {
        StationDTO dto = new StationDTO(
                "OUREN",
                "0071",
                5,
                "Ourense station description",
                "OURENSE",
                "22100",
                "0071,OUREN,22100",
                "OURENSE");

        assertEquals("OUREN", dto.stationCode());
        assertEquals("0071", dto.administrationCode());
        assertEquals(5, dto.priority());
        assertEquals("Ourense station description", dto.description());
        assertEquals("OURENSE", dto.stationName());
        assertEquals("22100", dto.uicCode());
        assertEquals("0071,OUREN,22100", dto.key());
        assertEquals("OURENSE", dto.stationNamePlano());
    }

    @Test
    void testRecordEquality()
    {
        StationDTO dto1 = new StationDTO(
                "MADRI",
                "0071",
                1,
                "Madrid",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");

        StationDTO dto2 = new StationDTO(
                "MADRI",
                "0071",
                1,
                "Madrid",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");

        // Records implement equals based on all fields
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordInequality()
    {
        StationDTO dto1 = new StationDTO(
                "MADRI",
                "0071",
                1,
                "Madrid",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");

        StationDTO dto2 = new StationDTO(
                "BARCE",
                "0071",
                1,
                "Barcelona",
                "BARCELONA (TODAS)",
                "71801",
                "0071,BARCE,null",
                "BARCELONA (TODAS)");

        assertNotEquals(dto1, dto2);
    }
}
