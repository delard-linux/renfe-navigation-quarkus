/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

/**
 * DTO representing a ticket purchase request.
 */
public class PurchaseTicketRequestDTO
{

    public String origin;
    public String destination;
    public String dateOut;
    public String dateReturn;
    public String adults;
    public String userName;
    public String serviceType;
    public String departureTime;
    public String fareName;
}
