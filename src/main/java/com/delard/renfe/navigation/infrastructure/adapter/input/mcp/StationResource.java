/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.mcp;


import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * MCP Tool for searching stations by text
 */
@ApplicationScoped
public class StationResource
{

    private static final Logger LOG = Logger.getLogger(StationResource.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    GetStationsUseCase getStationsUseCase;

    @Tool(description = "Search for stations by text. The search is case-insensitive and matches partial text in station names. Returns a list of matching stations with their codes, names, and other details. Minimum search text length is 3 characters.")
    public TextContent searchStations(
            @ToolArg(required = true,
                    description = "Search text (minimum 3 characters, case-insensitive). Searches in station names.") String search)
    {

        try {
            LOG.infof("[MCP TOOL] Request - searchStations: search='%s'", search);

            List<Station> stations = getStationsUseCase.searchStations(search);

            // Convert result to JSON string
            String resultJson = objectMapper.writeValueAsString(stations);

            LOG.infof("[MCP TOOL] Success - Found %d stations matching '%s'", stations.size(), search);

            return new TextContent(resultJson);

        } catch (ValidationException e) {
            LOG.warnf("[MCP TOOL] Validation error: %s", e.getMessage());
            return new TextContent("Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "[MCP TOOL] Error searching stations");
            return new TextContent("Error searching stations: " + e.getMessage());
        }
    }
}
