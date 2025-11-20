package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTicketServiceTest {

    private PurchaseTicketService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseTicketService();
    }

    @Test
    void shouldPurchaseTicketSuccessfully() {
        String message = service.purchaseTicket(
                "MADRID",
                "BARCELONA",
                "2026-01-16",
                "2026-01-20",
                "2",
                "John Doe",
                "AVE",
                "08:30",
                "Básico"
        );

        assertNotNull(message);
        assertTrue(message.contains("Ticket purchased"));
        assertTrue(message.contains("John Doe"));
        assertTrue(message.contains("MADRID -> BARCELONA"));
        assertTrue(message.contains("08:30"));
        assertTrue(message.contains("Le llegará un correo electrónico con los detalles."));
    }

    @Test
    void shouldValidateAdultsRange() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                service.purchaseTicket("MADRID", "BARCELONA", "2026-01-16", null,
                        "0", "User", "AVE", "08:30", "Básico"));
        assertEquals("Adults must be greater than 0", exception.getMessage());
    }

    @Test
    void shouldValidateDepartureTimeFormat() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                service.purchaseTicket("MADRID", "BARCELONA", "2026-01-16", null,
                        "1", "User", "AVE", "8:30", "Básico"));
        assertEquals("departureTime must be in HH:mm format", exception.getMessage());
    }

    @Test
    void shouldValidateDateFormat() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                service.purchaseTicket("MADRID", "BARCELONA", "16/01/2026", null,
                        "1", "User", "AVE", "08:30", "Básico"));
        assertTrue(exception.getMessage().contains("Invalid date format for dateOut"));
    }
}

