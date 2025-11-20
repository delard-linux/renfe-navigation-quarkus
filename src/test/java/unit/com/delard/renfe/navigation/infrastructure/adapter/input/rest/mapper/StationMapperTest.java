/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper;


import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.StationDTO;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for StationMapper
 */
class StationMapperTest
{

    @Test
    void testPrivateConstructor() throws Exception
    {
        Constructor<StationMapper> constructor = StationMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        StationMapper instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testToDTOWithNullStation()
    {
        StationDTO result = StationMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void testToDTOWithCompleteStation()
    {
        Station station = createCompleteStation();
        StationDTO dto = StationMapper.toDTO(station);

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
    void testToDTOWithNullFields()
    {
        Station station = new Station();
        station.setStationCode("BARCE");
        station.setAdministrationCode("0071");
        station.setPriority(null);
        station.setDescription(null);
        station.setStationName("BARCELONA (TODAS)");
        station.setUicCode(null);
        station.setKey("0071,BARCE,null");
        station.setStationNamePlano("BARCELONA (TODAS)");

        StationDTO dto = StationMapper.toDTO(station);

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
    void testToDTOListWithNullList()
    {
        List<StationDTO> result = StationMapper.toDTOList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDTOListWithEmptyList()
    {
        List<StationDTO> result = StationMapper.toDTOList(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDTOListWithSingleStation()
    {
        Station station = createCompleteStation();
        List<StationDTO> result = StationMapper.toDTOList(Arrays.asList(station));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MADRI", result.get(0).stationCode());
        assertEquals("MADRID (TODAS)", result.get(0).stationName());
    }

    @Test
    void testToDTOListWithMultipleStations()
    {
        Station station1 = createCompleteStation();
        Station station2 = new Station(
                "BARCE", "0071", 3, "Barcelona description",
                "BARCELONA (TODAS)", "71801", "0071,BARCE,null", "BARCELONA (TODAS)");
        Station station3 = new Station(
                "OUREN", "0071", 5, "Ourense description",
                "OURENSE", "22100", "0071,OUREN,22100", "OURENSE");

        List<StationDTO> result = StationMapper.toDTOList(Arrays.asList(station1, station2, station3));

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("MADRI", result.get(0).stationCode());
        assertEquals("BARCE", result.get(1).stationCode());
        assertEquals("OUREN", result.get(2).stationCode());
    }

    @Test
    void testToDTOListWithNullStationInList()
    {
        Station station1 = createCompleteStation();
        Station station2 = null;
        Station station3 = new Station(
                "OUREN", "0071", 5, "Ourense description",
                "OURENSE", "22100", "0071,OUREN,22100", "OURENSE");

        List<StationDTO> result = StationMapper.toDTOList(Arrays.asList(station1, station2, station3));

        assertNotNull(result);
        assertEquals(3, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
        assertNotNull(result.get(2));
    }

    @Test
    void testToDTOWithMinimalData()
    {
        Station station = new Station();
        station.setStationCode("TEST");
        station.setStationName("TEST STATION");

        StationDTO dto = StationMapper.toDTO(station);

        assertNotNull(dto);
        assertEquals("TEST", dto.stationCode());
        assertEquals("TEST STATION", dto.stationName());
        assertNull(dto.administrationCode());
        assertNull(dto.priority());
        assertNull(dto.description());
        assertNull(dto.uicCode());
        assertNull(dto.key());
        assertNull(dto.stationNamePlano());
    }

    private Station createCompleteStation()
    {
        return new Station(
                "MADRI",
                "0071",
                1,
                "Madrid description",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");
    }
}
