package com.delard.renfe.navigation.infrastructure.adapter.input.rest;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainsResponseDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper.TrainMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST Controller for train search operations
 */
@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Train Search", description = "Operations for searching train schedules and fares")
public class TrainResource {

    private static final Logger LOG = Logger.getLogger(TrainResource.class);

    @Inject
    SearchTrainsUseCase searchTrainsUseCase;

    @GET
    @Path("/trains")
    @Operation(summary = "Search trains", description = "Search for trains between two stations")
    @APIResponse(responseCode = "200", description = "Successful search with train results")
    @APIResponse(responseCode = "400", description = "Invalid request parameters")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getTrains(
            @Parameter(description = "Station origin (e.g., OURENSE)", required = true)
            @QueryParam("origin")
            @NotBlank(message = "Origin is required")
            String origin,

            @Parameter(description = "Station destination (e.g., MADRID)", required = true)
            @QueryParam("destination")
            @NotBlank(message = "Destination is required")
            String destination,

            @Parameter(description = "Outbound date in format YYYY-MM-DD", required = true)
            @QueryParam("date_out")
            @NotBlank(message = "Date out is required")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
            String dateOut,

            @Parameter(description = "Return date in format YYYY-MM-DD (optional)")
            @QueryParam("date_return")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
            String dateReturn,

            @Parameter(description = "Number of adult passengers (required, must be greater than 1, max 8)", required = true)
            @QueryParam("adults")
            @NotNull(message = "Adults is required")
            @Min(value = 2, message = "Adults must be greater than 1")
            @Max(value = 8, message = "Adults must be at most 8")
            Integer adults) {

        try {
            LOG.infof("REST Request - GET /trains: %s -> %s, dateOut: %s, dateReturn: %s, adults: %d",
                    origin, destination, dateOut, dateReturn, adults);

            TrainsResponse result = searchTrainsUseCase.searchTrains(
                    origin, destination, dateOut, dateReturn, adults);

            TrainsResponseDTO responseDTO = TrainMapper.toDTO(result);

            return Response.ok(responseDTO).build();

        } catch (ValidationException e) {
            LOG.warnf("Validation error in /trains request: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing /trains request");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Simple error response class
     */
    public static final class ErrorResponse {
        private String error;

        public ErrorResponse() {
        }

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}

