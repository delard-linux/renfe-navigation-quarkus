/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.port.input.PurchaseTicketUseCase;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.PurchaseTicketRequestDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.PurchaseTicketResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Unit tests for TicketPurchaseResource REST controller
 */
@ExtendWith(MockitoExtension.class)
class TicketPurchaseResourceTest
{

    @Mock
    private PurchaseTicketUseCase purchaseTicketUseCase;

    @InjectMocks
    private TicketPurchaseResource ticketPurchaseResource;

    @BeforeEach
    void setUp()
    {
        // Setup is handled by MockitoExtension
    }

    @Test
    @DisplayName("purchaseTicket should return 200 OK when purchase is successful")
    void testPurchaseTicketSuccess()
    {
        // Arrange
        PurchaseTicketRequestDTO request = createValidRequest();
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";

        when(purchaseTicketUseCase.purchaseTicket(
                eq(request.origin),
                eq(request.destination),
                eq(request.dateOut),
                eq(request.dateReturn),
                eq(request.adults),
                eq(request.userName),
                eq(request.serviceType),
                eq(request.departureTime),
                eq(request.fareName))).thenReturn(confirmation);

        // Act
        Response response = ticketPurchaseResource.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertInstanceOf(PurchaseTicketResponseDTO.class, response.getEntity());
        PurchaseTicketResponseDTO responseDTO = (PurchaseTicketResponseDTO)response.getEntity();
        assertEquals(confirmation, responseDTO.message);

        verify(purchaseTicketUseCase, times(1)).purchaseTicket(
                request.origin,
                request.destination,
                request.dateOut,
                request.dateReturn,
                request.adults,
                request.userName,
                request.serviceType,
                request.departureTime,
                request.fareName);
    }

    @Test
    @DisplayName("purchaseTicket should return 400 BAD_REQUEST when request is null")
    void testPurchaseTicketWithNullRequest()
    {
        // Act
        Response response = ticketPurchaseResource.purchaseTicket(null);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertInstanceOf(PurchaseTicketResponseDTO.class, response.getEntity());
        PurchaseTicketResponseDTO responseDTO = (PurchaseTicketResponseDTO)response.getEntity();
        assertTrue(responseDTO.message.contains("Request body is required"));

        verify(purchaseTicketUseCase, never()).purchaseTicket(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("purchaseTicket should return 400 BAD_REQUEST when ValidationException is thrown")
    void testPurchaseTicketWithValidationException()
    {
        // Arrange
        PurchaseTicketRequestDTO request = createValidRequest();
        ValidationException validationException = new ValidationException("Invalid date format");

        when(purchaseTicketUseCase.purchaseTicket(
                eq(request.origin),
                eq(request.destination),
                eq(request.dateOut),
                eq(request.dateReturn),
                eq(request.adults),
                eq(request.userName),
                eq(request.serviceType),
                eq(request.departureTime),
                eq(request.fareName))).thenThrow(validationException);

        // Act
        Response response = ticketPurchaseResource.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertInstanceOf(PurchaseTicketResponseDTO.class, response.getEntity());
        PurchaseTicketResponseDTO responseDTO = (PurchaseTicketResponseDTO)response.getEntity();
        assertEquals("Invalid date format", responseDTO.message);
    }

    @Test
    @DisplayName("purchaseTicket should return 500 INTERNAL_SERVER_ERROR when generic Exception is thrown")
    void testPurchaseTicketWithGenericException()
    {
        // Arrange
        PurchaseTicketRequestDTO request = createValidRequest();
        RuntimeException runtimeException = new RuntimeException("Database error");

        when(purchaseTicketUseCase.purchaseTicket(
                eq(request.origin),
                eq(request.destination),
                eq(request.dateOut),
                eq(request.dateReturn),
                eq(request.adults),
                eq(request.userName),
                eq(request.serviceType),
                eq(request.departureTime),
                eq(request.fareName))).thenThrow(runtimeException);

        // Act
        Response response = ticketPurchaseResource.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertInstanceOf(PurchaseTicketResponseDTO.class, response.getEntity());
        PurchaseTicketResponseDTO responseDTO = (PurchaseTicketResponseDTO)response.getEntity();
        assertEquals("Internal server error", responseDTO.message);
    }

    @Test
    @DisplayName("purchaseTicket should handle request with null dateReturn")
    void testPurchaseTicketWithNullDateReturn()
    {
        // Arrange
        PurchaseTicketRequestDTO request = createValidRequest();
        request.dateReturn = null;
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";

        when(purchaseTicketUseCase.purchaseTicket(
                eq(request.origin),
                eq(request.destination),
                eq(request.dateOut),
                isNull(),
                eq(request.adults),
                eq(request.userName),
                eq(request.serviceType),
                eq(request.departureTime),
                eq(request.fareName))).thenReturn(confirmation);

        // Act
        Response response = ticketPurchaseResource.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(purchaseTicketUseCase, times(1)).purchaseTicket(
                request.origin,
                request.destination,
                request.dateOut,
                null,
                request.adults,
                request.userName,
                request.serviceType,
                request.departureTime,
                request.fareName);
    }

    @Test
    @DisplayName("purchaseTicket should handle request with empty dateReturn")
    void testPurchaseTicketWithEmptyDateReturn()
    {
        // Arrange
        PurchaseTicketRequestDTO request = createValidRequest();
        request.dateReturn = "";
        String confirmation = "Ticket purchased successfully. Le llegará un correo electrónico con los detalles.";

        when(purchaseTicketUseCase.purchaseTicket(
                eq(request.origin),
                eq(request.destination),
                eq(request.dateOut),
                eq(""),
                eq(request.adults),
                eq(request.userName),
                eq(request.serviceType),
                eq(request.departureTime),
                eq(request.fareName))).thenReturn(confirmation);

        // Act
        Response response = ticketPurchaseResource.purchaseTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private PurchaseTicketRequestDTO createValidRequest()
    {
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO();
        request.origin = "MADRID (TODAS)";
        request.destination = "BARCELONA (TODAS)";
        request.dateOut = "2026-01-16";
        request.dateReturn = "2026-01-20";
        request.adults = "2";
        request.userName = "John Doe";
        request.serviceType = "AVE";
        request.departureTime = "08:00";
        request.fareName = "Básico";
        return request;
    }
}
