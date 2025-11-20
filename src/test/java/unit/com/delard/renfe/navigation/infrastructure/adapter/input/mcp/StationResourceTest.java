/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.mcp;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import io.quarkiverse.mcp.server.TextContent;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Unit tests for StationResource MCP tool
 */
@ExtendWith(MockitoExtension.class)
class StationResourceTest
{

    @Mock
    private GetStationsUseCase getStationsUseCase;

    @InjectMocks
    private StationResource stationResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp()
    {
        // Setup is handled by MockitoExtension
    }

    @Test
    void testSearchStationsSuccess() throws Exception
    {
        // Arrange
        String search = "MADRID";
        List<Station> stations = createMockStations();
        when(getStationsUseCase.searchStations(search)).thenReturn(stations);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());

        // Verify JSON is valid
        List<Station> parsedStations = objectMapper.readValue(
                result.text(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Station.class));
        assertEquals(1, parsedStations.size());
        assertEquals("MADRI", parsedStations.get(0).getStationCode());

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testSearchStationsWithEmptyResult() throws Exception
    {
        // Arrange
        String search = "NONEXISTENT";
        List<Station> emptyStations = new ArrayList<>();
        when(getStationsUseCase.searchStations(search)).thenReturn(emptyStations);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());

        // Verify JSON is valid and represents empty list
        List<Station> parsedStations = objectMapper.readValue(
                result.text(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Station.class));
        assertTrue(parsedStations.isEmpty());

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testSearchStationsWithValidationException()
    {
        // Arrange
        String search = "AB"; // Less than 3 characters
        ValidationException exception = new ValidationException("Search text must have at least 3 characters");
        when(getStationsUseCase.searchStations(search)).thenThrow(exception);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().startsWith("Error: "));
        assertTrue(result.text().contains("Search text must have at least 3 characters"));

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testSearchStationsWithRuntimeException()
    {
        // Arrange
        String search = "MADRID";
        RuntimeException exception = new RuntimeException("Service error");
        when(getStationsUseCase.searchStations(eq(search))).thenThrow(exception);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().startsWith("Error searching stations: "));
        assertTrue(result.text().contains("Service error"));
    }

    @Test
    void testSearchStationsWithNullPointerException()
    {
        // Arrange
        String search = "MADRID";
        NullPointerException exception = new NullPointerException("Null value");
        when(getStationsUseCase.searchStations(eq(search))).thenThrow(exception);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().startsWith("Error searching stations: "));
    }

    @Test
    void testSearchStationsWithMultipleResults() throws Exception
    {
        // Arrange
        String search = "MAD";
        List<Station> stations = createMultipleMockStations();
        when(getStationsUseCase.searchStations(search)).thenReturn(stations);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());

        // Verify JSON is valid and contains multiple stations
        List<Station> parsedStations = objectMapper.readValue(
                result.text(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Station.class));
        assertEquals(2, parsedStations.size());
        assertEquals("MADRI", parsedStations.get(0).getStationCode());
        assertEquals("MADRI2", parsedStations.get(1).getStationCode());

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testSearchStationsWithNullFields() throws Exception
    {
        // Arrange
        String search = "TEST";
        Station station = new Station();
        station.setStationCode("TEST");
        station.setStationName("TEST STATION");
        // Other fields are null
        List<Station> stations = new ArrayList<>();
        stations.add(station);
        when(getStationsUseCase.searchStations(search)).thenReturn(stations);

        // Act
        TextContent result = stationResource.searchStations(search);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());

        // Verify JSON is valid even with null fields
        List<Station> parsedStations = objectMapper.readValue(
                result.text(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Station.class));
        assertEquals(1, parsedStations.size());
        assertEquals("TEST", parsedStations.get(0).getStationCode());
        assertEquals("TEST STATION", parsedStations.get(0).getStationName());
    }

    private List<Station> createMockStations()
    {
        Station station = new Station(
                "MADRI",
                "0071",
                1,
                "Madrid description",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");
        List<Station> stations = new ArrayList<>();
        stations.add(station);
        return stations;
    }

    private List<Station> createMultipleMockStations()
    {
        Station station1 = new Station(
                "MADRI",
                "0071",
                1,
                "Madrid description",
                "MADRID (TODAS)",
                "71801",
                "0071,MADRI,null",
                "MADRID (TODAS)");
        Station station2 = new Station(
                "MADRI2",
                "0071",
                2,
                "Madrid Chamartin",
                "MADRID CHAMARTIN",
                "71802",
                "0071,MADRI2,71802",
                "MADRID CHAMARTIN");
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        return stations;
    }
}
