package com.delard.renfe.navigation.infrastructure.adapter.input.rest;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.port.input.PurchaseTicketUseCase;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.PurchaseTicketRequestDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.PurchaseTicketResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST resource for purchasing tickets.
 */
@ApplicationScoped
@Path("/tickets")
@Tag(name = "Ticket Purchase", description = "Operations for purchasing train tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TicketPurchaseResource {

    private static final Logger LOG = Logger.getLogger(TicketPurchaseResource.class);

    @Inject
    PurchaseTicketUseCase purchaseTicketUseCase;

    @POST
    @Path("/purchase")
    @Operation(summary = "Purchase ticket", description = "Validates data and simulates a ticket purchase")
    @APIResponse(responseCode = "200", description = "Ticket purchased successfully")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response purchaseTicket(PurchaseTicketRequestDTO request) {
        try {
            if (request == null) {
                throw new ValidationException("Request body is required");
            }

            String confirmation = purchaseTicketUseCase.purchaseTicket(
                    request.origin,
                    request.destination,
                    request.dateOut,
                    request.dateReturn,
                    request.adults,
                    request.userName,
                    request.serviceType,
                    request.departureTime,
                    request.fareName
            );

            return Response.ok(new PurchaseTicketResponseDTO(confirmation)).build();

        } catch (ValidationException e) {
            LOG.warnf("Validation error in /tickets/purchase: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PurchaseTicketResponseDTO(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing /tickets/purchase request");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new PurchaseTicketResponseDTO("Internal server error"))
                    .build();
        }
    }
}

