/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for PurchaseTicketRequestDTO
 */
class PurchaseTicketRequestDTOTest
{

    @Test
    void testDefaultConstructor()
    {
        PurchaseTicketRequestDTO dto = new PurchaseTicketRequestDTO();

        assertNotNull(dto);
        assertNull(dto.origin);
        assertNull(dto.destination);
        assertNull(dto.dateOut);
        assertNull(dto.dateReturn);
        assertNull(dto.adults);
        assertNull(dto.userName);
        assertNull(dto.serviceType);
        assertNull(dto.departureTime);
        assertNull(dto.fareName);
    }

    @Test
    void testSetAllFields()
    {
        PurchaseTicketRequestDTO dto = new PurchaseTicketRequestDTO();

        dto.origin = "MADRID (TODAS)";
        dto.destination = "BARCELONA (TODAS)";
        dto.dateOut = "2026-01-16";
        dto.dateReturn = "2026-01-20";
        dto.adults = "2";
        dto.userName = "John Doe";
        dto.serviceType = "AVE";
        dto.departureTime = "08:00";
        dto.fareName = "Básico";

        assertEquals("MADRID (TODAS)", dto.origin);
        assertEquals("BARCELONA (TODAS)", dto.destination);
        assertEquals("2026-01-16", dto.dateOut);
        assertEquals("2026-01-20", dto.dateReturn);
        assertEquals("2", dto.adults);
        assertEquals("John Doe", dto.userName);
        assertEquals("AVE", dto.serviceType);
        assertEquals("08:00", dto.departureTime);
        assertEquals("Básico", dto.fareName);
    }

    @Test
    void testSetFieldsWithNullValues()
    {
        PurchaseTicketRequestDTO dto = new PurchaseTicketRequestDTO();

        dto.origin = "MADRID";
        dto.destination = "BARCELONA";
        dto.dateOut = "2026-01-16";
        dto.dateReturn = null;
        dto.adults = "1";
        dto.userName = "Jane Doe";
        dto.serviceType = "ALVIA";
        dto.departureTime = "10:30";
        dto.fareName = "Promo";

        assertNotNull(dto.origin);
        assertNotNull(dto.destination);
        assertNotNull(dto.dateOut);
        assertNull(dto.dateReturn);
        assertNotNull(dto.adults);
        assertNotNull(dto.userName);
        assertNotNull(dto.serviceType);
        assertNotNull(dto.departureTime);
        assertNotNull(dto.fareName);
    }

    @Test
    void testSetFieldsWithEmptyStrings()
    {
        PurchaseTicketRequestDTO dto = new PurchaseTicketRequestDTO();

        dto.origin = "";
        dto.destination = "";
        dto.dateOut = "";
        dto.dateReturn = "";
        dto.adults = "";
        dto.userName = "";
        dto.serviceType = "";
        dto.departureTime = "";
        dto.fareName = "";

        assertEquals("", dto.origin);
        assertEquals("", dto.destination);
        assertEquals("", dto.dateOut);
        assertEquals("", dto.dateReturn);
        assertEquals("", dto.adults);
        assertEquals("", dto.userName);
        assertEquals("", dto.serviceType);
        assertEquals("", dto.departureTime);
        assertEquals("", dto.fareName);
    }
}
