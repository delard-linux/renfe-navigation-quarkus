/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.mcp;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.port.input.PurchaseTicketUseCase;

import org.jboss.logging.Logger;


/**
 * MCP Tool that exposes ticket purchase capabilities.
 */
@ApplicationScoped
public class TicketPurchaseResource
{

    private static final Logger LOG = Logger.getLogger(TicketPurchaseResource.class);

    @Inject
    PurchaseTicketUseCase purchaseTicketUseCase;

    @Tool(description = "Validate data and simulate the purchase of a train ticket.")
    public TextContent purchaseTicket(
            @ToolArg(required = true, description = "Origin station name") String origin,
            @ToolArg(required = true, description = "Destination station name") String destination,
            @ToolArg(required = true, description = "Outbound date in format YYYY-MM-DD") String dateOut,
            @ToolArg(required = false, description = "Return date in format YYYY-MM-DD") String dateReturn,
            @ToolArg(required = true, description = "Number of adult passengers (1-8)") String adults,
            @ToolArg(required = true, description = "User name") String userName,
            @ToolArg(required = true, description = "Service type (e.g., AVE)") String serviceType,
            @ToolArg(required = true, description = "Departure time (HH:mm)") String departureTime,
            @ToolArg(required = true, description = "Fare name (e.g., Básico)") String fareName)
    {
        try {
            LOG.infof("[MCP TOOL] Request - purchaseTicket: user=%s, %s -> %s", userName, origin, destination);
            String confirmation = purchaseTicketUseCase.purchaseTicket(
                    origin,
                    destination,
                    dateOut,
                    dateReturn,
                    adults,
                    userName,
                    serviceType,
                    departureTime,
                    fareName);
            return new TextContent(confirmation);
        } catch (ValidationException e) {
            LOG.warnf("[MCP TOOL] Validation error: %s", e.getMessage());
            return new TextContent("Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "[MCP TOOL] Error purchasing ticket");
            return new TextContent("Error purchasing ticket: " + e.getMessage());
        }
    }
}
