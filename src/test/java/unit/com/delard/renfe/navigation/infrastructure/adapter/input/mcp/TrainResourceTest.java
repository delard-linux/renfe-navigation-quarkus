/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.mcp;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import io.quarkiverse.mcp.server.TextContent;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Unit tests for TrainResource (MCP Tool)
 */
@ExtendWith(MockitoExtension.class)
class TrainResourceTest
{

    @Mock
    private SearchTrainsUseCase searchTrainsUseCase;

    @InjectMocks
    private TrainResource trainResource;

    private static final String ORIGIN = "MADRID (TODAS)";
    private static final String DESTINATION = "BARCELONA (TODAS)";
    private static final String DATE_OUT = "2026-01-16";
    private static final String DATE_RETURN = "2026-01-20";
    private static final String ADULTS = "2";

    @BeforeEach
    void setUp()
    {
        // TrainResource is already instantiated via @InjectMocks
    }

    @Test
    @DisplayName("getTrains should return JSON with train results when search is successful")
    void testGetTrainsSuccess() throws Exception
    {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        FareOption fare1 = new FareOption("Promo", 45.50, "EUR", "PROMO", null, null);
        train1.setFares(Arrays.asList(fare1));

        List<Train> trainsOut = Arrays.asList(train1);
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                trainsOut, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq(ADULTS)))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("TRAIN123"));
        assertTrue(result.text().contains("AVE"));
        assertTrue(result.text().contains("08:00"));
        assertTrue(result.text().contains("12:30"));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return JSON with train results when dateReturn is null")
    void testGetTrainsSuccessWithoutReturnDate() throws Exception
    {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trainsOut = Arrays.asList(train1);
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, null, ADULTS,
                trainsOut, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), isNull(), eq(ADULTS)))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, null, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("TRAIN123"));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, null, ADULTS);
    }

    @Test
    @DisplayName("getTrains should default adults to '1' when adults is null")
    void testGetTrainsWithNullAdults() throws Exception
    {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trainsOut = Arrays.asList(train1);
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "1",
                trainsOut, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq("1")))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        // Verify that "1" was passed to use case (default value)
        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "1");
    }

    @Test
    @DisplayName("getTrains should default adults to '1' when adults is blank")
    void testGetTrainsWithBlankAdults() throws Exception
    {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trainsOut = Arrays.asList(train1);
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "1",
                trainsOut, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq("1")))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "   ");

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        // Verify that "1" was passed to use case (default value)
        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "1");
    }

    @Test
    @DisplayName("getTrains should return JSON with both outbound and return trains")
    void testGetTrainsWithReturnTrains() throws Exception
    {
        // Arrange
        Train trainOut1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train trainRet1 = new Train("TRAIN456", "AVE", "16:00", "20:30", "4h 30m", 45.50);

        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<Train> trainsReturn = Arrays.asList(trainRet1);

        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                trainsOut, trainsReturn);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq(ADULTS)))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("TRAIN123"));
        assertTrue(result.text().contains("TRAIN456"));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return error message when ValidationException is thrown")
    void testGetTrainsWithValidationException() throws Exception
    {
        // Arrange
        String errorMessage = "Invalid date format";
        when(searchTrainsUseCase.searchTrains(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new ValidationException(errorMessage));

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Error:"));
        assertTrue(result.text().contains(errorMessage));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return error message when QueueException is thrown")
    void testGetTrainsWithQueueException() throws Exception
    {
        // Arrange
        String errorMessage = "Ticket purchase is queued";
        when(searchTrainsUseCase.searchTrains(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new QueueException(errorMessage));

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Error:"));
        assertTrue(result.text().contains(errorMessage));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return error message when general Exception is thrown")
    void testGetTrainsWithGeneralException() throws Exception
    {
        // Arrange
        String errorMessage = "Unexpected error occurred";
        when(searchTrainsUseCase.searchTrains(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Error searching trains:"));
        assertTrue(result.text().contains(errorMessage));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return JSON with empty trains list when no trains found")
    void testGetTrainsWithEmptyTrainsList() throws Exception
    {
        // Arrange
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                Arrays.asList(), null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq(ADULTS)))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        // Should still return valid JSON even with empty list
        assertTrue(result.text().contains("trainsOut"));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should handle null trainsOut in response")
    void testGetTrainsWithNullTrainsOut() throws Exception
    {
        // Arrange
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                null, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq(ADULTS)))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        // Should still return valid JSON even with null trainsOut
        assertTrue(result.text().contains("trainsOut"));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should preserve adults value when provided")
    void testGetTrainsWithAdultsValue() throws Exception
    {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trainsOut = Arrays.asList(train1);
        TrainsResponse response = new TrainsResponse(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "3",
                trainsOut, null);

        when(searchTrainsUseCase.searchTrains(eq(ORIGIN), eq(DESTINATION), eq(DATE_OUT), eq(DATE_RETURN), eq("3")))
                .thenReturn(response);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "3");

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        // Verify that "3" was passed to use case (not defaulted to "1")
        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, "3");
    }

    @Test
    @DisplayName("getTrains should return error message when TrainUnavailabilityException is thrown for outbound")
    void testGetTrainsWithTrainUnavailabilityExceptionOutbound() throws Exception
    {
        // Arrange
        String direction = "outbound";
        String detailMessage = "No hay trenes disponibles para la fecha seleccionada";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        when(searchTrainsUseCase.searchTrains(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(exception);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Error:"));
        assertTrue(result.text().contains("Error searching trains for outbound"));
        assertTrue(result.text().contains(detailMessage));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }

    @Test
    @DisplayName("getTrains should return error message when TrainUnavailabilityException is thrown for return")
    void testGetTrainsWithTrainUnavailabilityExceptionReturn() throws Exception
    {
        // Arrange
        String direction = "return";
        String detailMessage = "No hay billetes de vuelta disponibles";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        when(searchTrainsUseCase.searchTrains(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(exception);

        // Act
        TextContent result = trainResource.getTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Error:"));
        assertTrue(result.text().contains("Error searching trains for return"));
        assertTrue(result.text().contains(detailMessage));

        verify(searchTrainsUseCase, times(1)).searchTrains(ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS);
    }
}
