package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.port.input.PurchaseTicketUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Application service responsible for validating ticket purchase data and returning confirmations.
 */
@ApplicationScoped
public class PurchaseTicketService implements PurchaseTicketUseCase {

    private static final Logger LOG = Logger.getLogger(PurchaseTicketService.class);

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EXPECTED_DATE_FORMAT = "yyyy-MM-dd (e.g., 2026-01-16)";
    private static final DateTimeFormatter DEPARTURE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String purchaseTicket(String origin,
                                 String destination,
                                 String dateOut,
                                 String dateReturn,
                                 String adults,
                                 String userName,
                                 String serviceType,
                                 String departureTime,
                                 String fareName) {

        LOG.debugf("[REQUEST] Purchase ticket: %s -> %s, dateOut: %s, dateReturn: %s, adults: %s, user: %s, service: %s, departure: %s, fare: %s",
                origin, destination, dateOut, dateReturn, adults, userName, serviceType, departureTime, fareName);

        String sanitizedOrigin = requireNonBlank(origin, "origin");
        String sanitizedDestination = requireNonBlank(destination, "destination");
        String sanitizedAdults = validateAdults(adults);
        String sanitizedUser = requireNonBlank(userName, "user");
        String sanitizedServiceType = requireNonBlank(serviceType, "serviceType");
        String sanitizedDepartureTime = validateDepartureTime(departureTime);
        String sanitizedFareName = requireNonBlank(fareName, "fareName");

        String formattedDateOut = validateAndFormatDate(dateOut, "dateOut");
        String formattedDateReturn = null;
        if (dateReturn != null && !dateReturn.isBlank()) {
            formattedDateReturn = validateAndFormatDate(dateReturn, "dateReturn");
        }

        StringBuilder confirmation = new StringBuilder();
        confirmation.append(String.format(
                "Ticket purchased for %s: %s -> %s on %s",
                sanitizedUser,
                sanitizedOrigin,
                sanitizedDestination,
                formattedDateOut
        ));

        if (formattedDateReturn != null) {
            confirmation.append(String.format(" with return on %s", formattedDateReturn));
        }

        confirmation.append(String.format(
                ". Service: %s at %s, fare: %s, adults: %s. Le llegará un correo electrónico con los detalles.",
                sanitizedServiceType,
                sanitizedDepartureTime,
                sanitizedFareName,
                sanitizedAdults
        ));

        LOG.infof("[SUCCESS] Ticket purchase simulated for user %s", sanitizedUser);
        return confirmation.toString();
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(String.format("%s is required", fieldName));
        }
        return value.trim();
    }

    private String validateAdults(String adults) {
        String sanitized = requireNonBlank(adults, "adults");
        try {
            int adultsInt = Integer.parseInt(sanitized.trim());
            if (adultsInt <= 0) {
                throw new ValidationException("Adults must be greater than 0");
            }
            if (adultsInt > 8) {
                throw new ValidationException("Adults must be at most 8");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Adults must be a valid number");
        }
        return sanitized.trim();
    }

    private String validateDepartureTime(String departureTime) {
        String sanitized = requireNonBlank(departureTime, "departureTime");
        try {
            LocalTime.parse(sanitized, DEPARTURE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("departureTime must be in HH:mm format");
        }
        return sanitized;
    }

    private String validateAndFormatDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new ValidationException(String.format("%s is required", fieldName));
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, INPUT_DATE_FORMAT);
            String formatted = date.format(OUTPUT_DATE_FORMAT);
            LOG.debugf("Formatted %s from '%s' to '%s'", fieldName, dateStr, formatted);
            return formatted;
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    String.format(
                            "Invalid date format for %s: '%s'. Expected format: %s",
                            fieldName,
                            dateStr,
                            EXPECTED_DATE_FORMAT));
        }
    }
}

