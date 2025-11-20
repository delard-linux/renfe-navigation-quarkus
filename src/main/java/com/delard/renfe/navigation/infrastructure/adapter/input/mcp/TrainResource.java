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

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * MCP Tool for searching trains between two stations
 */
@ApplicationScoped
public class TrainResource
{

    private static final Logger LOG = Logger.getLogger(TrainResource.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    SearchTrainsUseCase searchTrainsUseCase;

    @Tool(description = "Search for train schedules and fares between two stations. Returns available trains with departure/arrival times, prices, and fare options.")
    public TextContent getTrains(
            @ToolArg(required = true, description = "Origin station name (e.g., OURENSE, MADRID)") String origin,
            @ToolArg(required = true,
                    description = "Destination station name (e.g., MADRID, OURENSE)") String destination,
            @ToolArg(required = true,
                    description = "Departure date in format YYYY-MM-DD (e.g., 2026-01-16)") String dateOut,
            @ToolArg(required = false,
                    description = "Return date in format YYYY-MM-DD (e.g., 2026-01-20). Optional for one-way trips.") String dateReturn,
            @ToolArg(required = false,
                    description = "Number of adult passengers (1-8). Defaults to 1 if not specified.") String adults)
    {

        try {
            LOG.infof("[MCP TOOL] Request - getTrains: %s -> %s, dateOut: %s, dateReturn: %s, adults: %s",
                    origin, destination, dateOut, dateReturn, adults);

            // Default adults to "1" if not provided
            String adultsParam = (adults == null || adults.isBlank()) ? "1" : adults;

            TrainsResponse result = searchTrainsUseCase.searchTrains(
                    origin, destination, dateOut, dateReturn, adultsParam);

            // Convert result to JSON string
            String resultJson = objectMapper.writeValueAsString(result);

            LOG.infof("[MCP TOOL] Success - Found %d outbound trains",
                    result.getTrainsOut() != null ? result.getTrainsOut().size() : 0);

            return new TextContent(resultJson);

        } catch (ValidationException e) {
            LOG.warnf("[MCP TOOL] Validation error: %s", e.getMessage());
            return new TextContent("Error: " + e.getMessage());
        } catch (TrainUnavailabilityException e) {
            LOG.warnf("[MCP TOOL] Train unavailability: %s", e.getMessage());
            return new TextContent("Error: " + e.getMessage());
        } catch (QueueException e) {
            LOG.warnf("[MCP TOOL] Queue error: %s", e.getMessage());
            return new TextContent("Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "[MCP TOOL] Error searching trains");
            return new TextContent("Error searching trains: " + e.getMessage());
        }
    }
}
