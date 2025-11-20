/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Unit tests for StationResource REST controller
 */
@ExtendWith(MockitoExtension.class)
class StationResourceTest
{

    @Mock
    private GetStationsUseCase getStationsUseCase;

    @InjectMocks
    private StationResource stationResource;

    @BeforeEach
    void setUp()
    {
        // Setup is handled by MockitoExtension
    }

    @Test
    void testSearchStationsSuccess()
    {
        // Arrange
        String search = "MADRID";
        List<Station> stations = createMockStations();
        when(getStationsUseCase.searchStations(search)).thenReturn(stations);

        // Act
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testSearchStationsWithEmptyResult()
    {
        // Arrange
        String search = "NONEXISTENT";
        List<Station> emptyStations = new ArrayList<>();
        when(getStationsUseCase.searchStations(search)).thenReturn(emptyStations);

        // Act
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

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
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        StationResource.ErrorResponse errorResponse = (StationResource.ErrorResponse)response.getEntity();
        assertNotNull(errorResponse.getError());
        assertEquals("Search text must have at least 3 characters", errorResponse.getError());

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
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        StationResource.ErrorResponse errorResponse = (StationResource.ErrorResponse)response.getEntity();
        assertNotNull(errorResponse.getError());
        assertEquals("Service error", errorResponse.getError());
    }

    @Test
    void testSearchStationsWithNullPointerException()
    {
        // Arrange
        String search = "MADRID";
        NullPointerException exception = new NullPointerException("Null value");
        when(getStationsUseCase.searchStations(eq(search))).thenThrow(exception);

        // Act
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void testSearchStationsWithMultipleResults()
    {
        // Arrange
        String search = "MAD";
        List<Station> stations = createMultipleMockStations();
        when(getStationsUseCase.searchStations(search)).thenReturn(stations);

        // Act
        Response response = stationResource.searchStations(search);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        verify(getStationsUseCase, times(1)).searchStations(search);
    }

    @Test
    void testErrorResponseDefaultConstructor()
    {
        // Arrange & Act
        StationResource.ErrorResponse errorResponse = new StationResource.ErrorResponse();

        // Assert
        assertNotNull(errorResponse);
        assertNull(errorResponse.getError());
    }

    @Test
    void testErrorResponseWithMessage()
    {
        // Arrange
        String errorMessage = "Test error message";

        // Act
        StationResource.ErrorResponse errorResponse = new StationResource.ErrorResponse(errorMessage);

        // Assert
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getError());
    }

    @Test
    void testErrorResponseSetterAndGetter()
    {
        // Arrange
        StationResource.ErrorResponse errorResponse = new StationResource.ErrorResponse();
        String errorMessage = "New error message";

        // Act
        errorResponse.setError(errorMessage);

        // Assert
        assertEquals(errorMessage, errorResponse.getError());
    }

    @Test
    void testErrorResponseWithNullMessage()
    {
        // Arrange
        StationResource.ErrorResponse errorResponse = new StationResource.ErrorResponse(null);

        // Assert
        assertNotNull(errorResponse);
        assertNull(errorResponse.getError());
    }

    @Test
    void testErrorResponseWithEmptyMessage()
    {
        // Arrange
        StationResource.ErrorResponse errorResponse = new StationResource.ErrorResponse("");

        // Assert
        assertNotNull(errorResponse);
        assertEquals("", errorResponse.getError());
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
