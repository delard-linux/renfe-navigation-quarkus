/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

/**
 * DTO containing the response of a ticket purchase operation.
 */
public class PurchaseTicketResponseDTO
{
    public String message;

    public PurchaseTicketResponseDTO()
    {
    }

    public PurchaseTicketResponseDTO(String message)
    {
        this.message = message;
    }
}
