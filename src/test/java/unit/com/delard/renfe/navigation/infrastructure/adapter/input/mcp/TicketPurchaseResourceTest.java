package com.delard.renfe.navigation.infrastructure.adapter.input.mcp;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.port.input.PurchaseTicketUseCase;
import io.quarkiverse.mcp.server.TextContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketPurchaseResource (MCP Tool)
 */
@ExtendWith(MockitoExtension.class)
class TicketPurchaseResourceTest {

    @Mock
    private PurchaseTicketUseCase purchaseTicketUseCase;

    @InjectMocks
    private TicketPurchaseResource ticketPurchaseResource;

    private static final String ORIGIN = "MADRID (TODAS)";
    private static final String DESTINATION = "BARCELONA (TODAS)";
    private static final String DATE_OUT = "2026-01-16";
    private static final String DATE_RETURN = "2026-01-20";
    private static final String ADULTS = "2";
    private static final String USER_NAME = "John Doe";
    private static final String SERVICE_TYPE = "AVE";
    private static final String DEPARTURE_TIME = "08:00";
    private static final String FARE_NAME = "Básico";

    @BeforeEach
    void setUp() {
        // TicketPurchaseResource is already instantiated via @InjectMocks
    }

    @Test
    @DisplayName("purchaseTicket should return confirmation message when purchase is successful")
    void testPurchaseTicketSuccess() {
        // Arrange
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";
        
        when(purchaseTicketUseCase.purchaseTicket(
                eq(ORIGIN),
                eq(DESTINATION),
                eq(DATE_OUT),
                eq(DATE_RETURN),
                eq(ADULTS),
                eq(USER_NAME),
                eq(SERVICE_TYPE),
                eq(DEPARTURE_TIME),
                eq(FARE_NAME)
        )).thenReturn(confirmation);

        // Act
        TextContent result = ticketPurchaseResource.purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertEquals(confirmation, result.text());
        
        verify(purchaseTicketUseCase, times(1)).purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );
    }

    @Test
    @DisplayName("purchaseTicket should return confirmation message when dateReturn is null")
    void testPurchaseTicketSuccessWithoutReturnDate() {
        // Arrange
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";
        
        when(purchaseTicketUseCase.purchaseTicket(
                eq(ORIGIN),
                eq(DESTINATION),
                eq(DATE_OUT),
                isNull(),
                eq(ADULTS),
                eq(USER_NAME),
                eq(SERVICE_TYPE),
                eq(DEPARTURE_TIME),
                eq(FARE_NAME)
        )).thenReturn(confirmation);

        // Act
        TextContent result = ticketPurchaseResource.purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, null, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertEquals(confirmation, result.text());
        
        verify(purchaseTicketUseCase, times(1)).purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, null, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );
    }

    @Test
    @DisplayName("purchaseTicket should return error message when ValidationException is thrown")
    void testPurchaseTicketWithValidationException() {
        // Arrange
        ValidationException validationException = new ValidationException("Invalid date format");
        
        when(purchaseTicketUseCase.purchaseTicket(
                eq(ORIGIN),
                eq(DESTINATION),
                eq(DATE_OUT),
                eq(DATE_RETURN),
                eq(ADULTS),
                eq(USER_NAME),
                eq(SERVICE_TYPE),
                eq(DEPARTURE_TIME),
                eq(FARE_NAME)
        )).thenThrow(validationException);

        // Act
        TextContent result = ticketPurchaseResource.purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().startsWith("Error: "));
        assertTrue(result.text().contains("Invalid date format"));
    }

    @Test
    @DisplayName("purchaseTicket should return error message when generic Exception is thrown")
    void testPurchaseTicketWithGenericException() {
        // Arrange
        RuntimeException runtimeException = new RuntimeException("Database error");
        
        when(purchaseTicketUseCase.purchaseTicket(
                eq(ORIGIN),
                eq(DESTINATION),
                eq(DATE_OUT),
                eq(DATE_RETURN),
                eq(ADULTS),
                eq(USER_NAME),
                eq(SERVICE_TYPE),
                eq(DEPARTURE_TIME),
                eq(FARE_NAME)
        )).thenThrow(runtimeException);

        // Act
        TextContent result = ticketPurchaseResource.purchaseTicket(
                ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, FARE_NAME
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().startsWith("Error purchasing ticket: "));
        assertTrue(result.text().contains("Database error"));
    }

    @Test
    @DisplayName("purchaseTicket should handle different service types")
    void testPurchaseTicketWithDifferentServiceTypes() {
        // Arrange
        String[] serviceTypes = {"AVE", "ALVIA", "EUROMED", "AVLO"};
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";
        
        for (String serviceType : serviceTypes) {
            when(purchaseTicketUseCase.purchaseTicket(
                    eq(ORIGIN),
                    eq(DESTINATION),
                    eq(DATE_OUT),
                    eq(DATE_RETURN),
                    eq(ADULTS),
                    eq(USER_NAME),
                    eq(serviceType),
                    eq(DEPARTURE_TIME),
                    eq(FARE_NAME)
            )).thenReturn(confirmation);

            // Act
            TextContent result = ticketPurchaseResource.purchaseTicket(
                    ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                    USER_NAME, serviceType, DEPARTURE_TIME, FARE_NAME
            );

            // Assert
            assertNotNull(result);
            assertEquals(confirmation, result.text());
        }
    }

    @Test
    @DisplayName("purchaseTicket should handle different fare names")
    void testPurchaseTicketWithDifferentFareNames() {
        // Arrange
        String[] fareNames = {"Básico", "Promo", "Premium", "Flexible"};
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";
        
        for (String fareName : fareNames) {
            when(purchaseTicketUseCase.purchaseTicket(
                    eq(ORIGIN),
                    eq(DESTINATION),
                    eq(DATE_OUT),
                    eq(DATE_RETURN),
                    eq(ADULTS),
                    eq(USER_NAME),
                    eq(SERVICE_TYPE),
                    eq(DEPARTURE_TIME),
                    eq(fareName)
            )).thenReturn(confirmation);

            // Act
            TextContent result = ticketPurchaseResource.purchaseTicket(
                    ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                    USER_NAME, SERVICE_TYPE, DEPARTURE_TIME, fareName
            );

            // Assert
            assertNotNull(result);
            assertEquals(confirmation, result.text());
        }
    }

    @Test
    @DisplayName("purchaseTicket should handle different departure times")
    void testPurchaseTicketWithDifferentDepartureTimes() {
        // Arrange
        String[] departureTimes = {"08:00", "10:30", "14:15", "20:45"};
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";
        
        for (String departureTime : departureTimes) {
            when(purchaseTicketUseCase.purchaseTicket(
                    eq(ORIGIN),
                    eq(DESTINATION),
                    eq(DATE_OUT),
                    eq(DATE_RETURN),
                    eq(ADULTS),
                    eq(USER_NAME),
                    eq(SERVICE_TYPE),
                    eq(departureTime),
                    eq(FARE_NAME)
            )).thenReturn(confirmation);

            // Act
            TextContent result = ticketPurchaseResource.purchaseTicket(
                    ORIGIN, DESTINATION, DATE_OUT, DATE_RETURN, ADULTS,
                    USER_NAME, SERVICE_TYPE, departureTime, FARE_NAME
            );

            // Assert
            assertNotNull(result);
            assertEquals(confirmation, result.text());
        }
    }
}

