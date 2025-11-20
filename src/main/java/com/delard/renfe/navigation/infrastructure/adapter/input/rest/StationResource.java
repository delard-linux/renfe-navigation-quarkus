/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest;


import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.StationDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper.StationMapper;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;


/**
 * REST Controller for station search operations
 */
@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Station Search", description = "Operations for searching stations")
public class StationResource
{

    private static final Logger LOG = Logger.getLogger(StationResource.class);

    @Inject
    GetStationsUseCase getStationsUseCase;

    @GET
    @Path("/stations")
    @Operation(summary = "Search stations",
            description = "Search for stations by text. The search is case-insensitive and matches partial text in station names.")
    @APIResponse(responseCode = "200", description = "Successful search with station results")
    @APIResponse(responseCode = "400",
            description = "Invalid request parameters (search text must have at least 3 characters)")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response searchStations(
            @Parameter(description = "Search text (minimum 3 characters, case-insensitive)",
                    required = true) @QueryParam("search") @NotBlank(message = "Search text is required") String search)
    {

        try {
            LOG.infof("REST Request - GET /stations: search='%s'", search);

            List<Station> stations = getStationsUseCase.searchStations(search);

            List<StationDTO> responseDTOs = StationMapper.toDTOList(stations);

            LOG.infof("REST Response - Found %d stations matching '%s'", stations.size(), search);

            return Response.ok(responseDTOs).build();

        } catch (ValidationException e) {
            LOG.warnf("Validation error in /stations request: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing /stations request");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Simple error response class
     */
    public static final class ErrorResponse
    {
        private String error;

        public ErrorResponse()
        {
        }

        public ErrorResponse(String error)
        {
            this.error = error;
        }

        public String getError()
        {
            return error;
        }

        public void setError(String error)
        {
            this.error = error;
        }
    }
}
