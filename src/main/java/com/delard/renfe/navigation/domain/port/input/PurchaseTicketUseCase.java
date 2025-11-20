/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.port.input;

/**
 * Use case for purchasing tickets.
 */
public interface PurchaseTicketUseCase
{

    /**
     * Attempts to purchase a ticket with the provided data.
     *
     * @param origin         Origin station
     * @param destination    Destination station
     * @param dateOut        Outbound date (yyyy-MM-dd)
     * @param dateReturn     Return date (optional, yyyy-MM-dd)
     * @param adults         Number of adult passengers (string representing integer)
     * @param userName       Name of the user purchasing the ticket
     * @param serviceType    Train service type (e.g., AVE)
     * @param departureTime  Departure time in HH:mm format
     * @param fareName       Fare name (e.g., Básico)
     * @return Confirmation message
     */
    String purchaseTicket(String origin,
            String destination,
            String dateOut,
            String dateReturn,
            String adults,
            String userName,
            String serviceType,
            String departureTime,
            String fareName);
}
