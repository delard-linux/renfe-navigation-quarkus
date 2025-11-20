package com.delard.renfe.navigation.infrastructure.adapter.input.rest;

import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrainResource REST controller
 */
@ExtendWith(MockitoExtension.class)
class TrainResourceTest {

    @Mock
    private SearchTrainsUseCase searchTrainsUseCase;

    @InjectMocks
    private TrainResource trainResource;

    @BeforeEach
    void setUp() {
        // Setup is handled by MockitoExtension
    }

    @Test
    void testGetTrainsSuccess() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        String adults = "2";

        TrainsResponse trainsResponse = createMockTrainsResponse();
        when(searchTrainsUseCase.searchTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(trainsResponse);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    void testGetTrainsSuccessWithoutReturnDate() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "1";

        TrainsResponse trainsResponse = createMockTrainsResponse();
        when(searchTrainsUseCase.searchTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(trainsResponse);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void testGetTrainsWithEmptyReturnDate() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "";
        String adults = "1";

        TrainsResponse trainsResponse = createMockTrainsResponse();
        when(searchTrainsUseCase.searchTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(trainsResponse);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetTrainsWithException() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "1";

        RuntimeException exception = new RuntimeException("Service error");
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        TrainResource.ErrorResponse errorResponse = (TrainResource.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse.getError());
        assertEquals("Service error", errorResponse.getError());
    }

    @Test
    void testGetTrainsWithNullPointerException() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "1";

        NullPointerException exception = new NullPointerException("Null value");
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void testGetTrainsWithDifferentAdultsCount() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "8"; // Maximum

        TrainsResponse trainsResponse = createMockTrainsResponse();
        when(searchTrainsUseCase.searchTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(trainsResponse);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, "8");
    }

    @Test
    void testErrorResponseDefaultConstructor() {
        // Arrange & Act
        TrainResource.ErrorResponse errorResponse = new TrainResource.ErrorResponse();

        // Assert
        assertNotNull(errorResponse);
        assertNull(errorResponse.getError());
    }

    @Test
    void testErrorResponseWithMessage() {
        // Arrange
        String errorMessage = "Test error message";

        // Act
        TrainResource.ErrorResponse errorResponse = new TrainResource.ErrorResponse(errorMessage);

        // Assert
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getError());
    }

    @Test
    void testErrorResponseSetterAndGetter() {
        // Arrange
        TrainResource.ErrorResponse errorResponse = new TrainResource.ErrorResponse();
        String errorMessage = "New error message";

        // Act
        errorResponse.setError(errorMessage);

        // Assert
        assertEquals(errorMessage, errorResponse.getError());
    }

    @Test
    void testErrorResponseWithNullMessage() {
        // Arrange
        TrainResource.ErrorResponse errorResponse = new TrainResource.ErrorResponse(null);

        // Assert
        assertNotNull(errorResponse);
        assertNull(errorResponse.getError());
    }

    @Test
    void testErrorResponseWithEmptyMessage() {
        // Arrange
        TrainResource.ErrorResponse errorResponse = new TrainResource.ErrorResponse("");

        // Assert
        assertNotNull(errorResponse);
        assertEquals("", errorResponse.getError());
    }

    @Test
    @DisplayName("Should return HTTP 404 when TrainUnavailabilityException is thrown for outbound trains")
    void testGetTrainsWithTrainUnavailabilityExceptionOutbound() {
        // Arrange
        String origin = "MADRID-RECOLETOS";
        String destination = "BARCELONA (TODAS)";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        String adults = "1";
        
        String direction = "outbound";
        String detailMessage = "No hay trenes disponibles para la fecha seleccionada";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);
        
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        TrainResource.ErrorResponse errorResponse = (TrainResource.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse.getError());
        assertTrue(errorResponse.getError().contains("Error searching trains for outbound"));
        assertTrue(errorResponse.getError().contains(detailMessage));
        
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should return HTTP 404 when TrainUnavailabilityException is thrown for return trains")
    void testGetTrainsWithTrainUnavailabilityExceptionReturn() {
        // Arrange
        String origin = "MADRID (TODAS)";
        String destination = "BARCELONA (TODAS)";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        String adults = "2";
        
        String direction = "return";
        String detailMessage = "No hay billetes de vuelta disponibles";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);
        
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        TrainResource.ErrorResponse errorResponse = (TrainResource.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse.getError());
        assertTrue(errorResponse.getError().contains("Error searching trains for return"));
        assertTrue(errorResponse.getError().contains(detailMessage));
        
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should return HTTP 400 when ValidationException is thrown")
    void testGetTrainsWithValidationException() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "invalid-date";
        String dateReturn = null;
        String adults = "1";
        
        ValidationException exception = new ValidationException("Invalid date format");
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        TrainResource.ErrorResponse errorResponse = (TrainResource.ErrorResponse) response.getEntity();
        assertEquals("Invalid date format", errorResponse.getError());
        
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should return HTTP 503 when QueueException is thrown")
    void testGetTrainsWithQueueException() {
        // Arrange
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String adults = "1";
        
        QueueException exception = new QueueException("Ticket purchase is queued");
        when(searchTrainsUseCase.searchTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), eq(adults)))
                .thenThrow(exception);

        // Act
        Response response = trainResource.getTrains(origin, destination, dateOut, dateReturn, adults);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        
        TrainResource.ErrorResponse errorResponse = (TrainResource.ErrorResponse) response.getEntity();
        assertEquals("Ticket purchase is queued", errorResponse.getError());
        
        verify(searchTrainsUseCase, times(1))
                .searchTrains(origin, destination, dateOut, dateReturn, adults);
    }

    private TrainsResponse createMockTrainsResponse() {
        TrainsResponse response = new TrainsResponse();
        response.setOrigin("OURENSE");
        response.setDestination("MADRID");
        response.setDateOut("2025-12-01");
        response.setDateReturn("2025-12-05");
        response.setAdults("2");

        Train train = new Train();
        train.setTrainId("TRAIN123");
        train.setServiceType("AVE");
        train.setDepartureTime("08:00");
        train.setArrivalTime("12:30");
        train.setDuration("4h 30m");
        train.setPriceFrom(45.50);

        List<Train> trains = new ArrayList<>();
        trains.add(train);
        response.setTrainsOut(trains);
        response.setTrainsReturn(null);

        return response;
    }
}

