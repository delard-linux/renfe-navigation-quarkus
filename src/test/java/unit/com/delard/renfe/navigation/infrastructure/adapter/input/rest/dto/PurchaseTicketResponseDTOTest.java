/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for PurchaseTicketResponseDTO
 */
class PurchaseTicketResponseDTOTest
{

    @Test
    void testDefaultConstructor()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO();

        assertNotNull(dto);
        assertNull(dto.message);
    }

    @Test
    void testConstructorWithMessage()
    {
        String message = "Ticket purchased successfully";
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO(message);

        assertNotNull(dto);
        assertEquals(message, dto.message);
    }

    @Test
    void testSetMessage()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO();

        String message = "Le llegará un correo electrónico con los detalles";
        dto.message = message;

        assertEquals(message, dto.message);
    }

    @Test
    void testSetMessageToNull()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO("Initial message");

        dto.message = null;

        assertNull(dto.message);
    }

    @Test
    void testSetMessageToEmptyString()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO("Initial message");

        dto.message = "";

        assertEquals("", dto.message);
    }

    @Test
    void testConstructorWithNullMessage()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO(null);

        assertNotNull(dto);
        assertNull(dto.message);
    }

    @Test
    void testConstructorWithEmptyMessage()
    {
        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO("");

        assertNotNull(dto);
        assertEquals("", dto.message);
    }

    @Test
    void testLongMessage()
    {
        String longMessage = "Ticket purchased successfully. " +
                "Origin: MADRID (TODAS), Destination: BARCELONA (TODAS), " +
                "Date: 2026-01-16, Service: AVE, Departure: 08:00, Fare: Básico. " +
                "Le llegará un correo electrónico con los detalles.";

        PurchaseTicketResponseDTO dto = new PurchaseTicketResponseDTO(longMessage);

        assertEquals(longMessage, dto.message);
    }
}
