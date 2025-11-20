/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.application.service;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;

import org.jboss.logging.Logger;


/**
 * Application service for searching trains
 */
@ApplicationScoped
public class SearchTrainsService implements SearchTrainsUseCase
{

    private static final Logger LOG = Logger.getLogger(SearchTrainsService.class);

    // Date format constants
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EXPECTED_DATE_FORMAT = "yyyy-MM-dd (e.g., 2026-01-16)";

    @Inject
    TrainScraperPort trainScraperPort;

    @Inject
    GetStationsUseCase getStationsUseCase;

    @Override
    public TrainsResponse searchTrains(String origin, String destination, String dateOut,
            String dateReturn, String adults)
    {
        Instant startTime = Instant.now();

        // Validate required parameters
        validateRequiredFields(origin, destination, dateOut, adults);

        // Validate and format dates
        String formattedDateOut = validateAndFormatDate(dateOut, "dateOut");
        String formattedDateReturn = dateReturn != null && !dateReturn.isBlank()
                ? validateAndFormatDate(dateReturn, "dateReturn")
                : null;

        // Validate stations exist and are unique, get station data
        StationValidationResult stationValidation = validateStations(origin, destination);
        String realOrigin = stationValidation.getOriginStationName();
        String realDestination = stationValidation.getDestinationStationName();
        String originDesgEstacion = stationValidation.getOriginDesgEstacion();
        String destinationDesgEstacion = stationValidation.getDestinationDesgEstacion();
        String originClave = stationValidation.getOriginClave();
        String destinationClave = stationValidation.getDestinationClave();

        LOG.debugf("[REQUEST] Starting search: %s -> %s, Outbound: %s, Return: %s, Passengers: %s",
                realOrigin, realDestination, formattedDateOut, formattedDateReturn, adults);

        try {
            List<List<Train>> result = trainScraperPort.scrapeTrains(
                    realOrigin, realDestination, originDesgEstacion, destinationDesgEstacion,
                    originClave, destinationClave, formattedDateOut, formattedDateReturn, adults);

            List<Train> trainsOut = result.get(0);
            List<Train> trainsReturn = result.size() > 1 ? result.get(1) : null;

            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.debugf("[SUCCESS] Search completed in %.2fs - Outbound trains: %d, Return trains: %d",
                    elapsed.toMillis() / 1000.0,
                    trainsOut != null ? trainsOut.size() : 0,
                    trainsReturn != null ? trainsReturn.size() : 0);

            return new TrainsResponse(
                    realOrigin, realDestination, formattedDateOut, formattedDateReturn, adults,
                    trainsOut, trainsReturn);

        } catch (QueueException e) {
            // Re-throw queue exceptions as-is
            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.warnf("[WARN] Queue detected after %.2fs: %s",
                    elapsed.toMillis() / 1000.0, e.getMessage());
            throw e;
        } catch (TrainUnavailabilityException e) {
            // Re-throw train unavailability exceptions as-is
            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.warnf("[WARN] No trains available after %.2fs - %s: %s",
                    elapsed.toMillis() / 1000.0, e.getDirection(), e.getDetailMessage());
            throw e;
        } catch (Exception e) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.errorf(e, "[ERROR] Search failed after %.2fs: %s",
                    elapsed.toMillis() / 1000.0, e.getMessage());
            throw new RuntimeException("Error searching trains: " + e.getMessage(), e);
        }
    }

    /**
     * Validates and formats a date string from yyyy-MM-dd to dd/MM/yyyy
     *
     * @param dateStr  Date string to validate and format
     * @param fieldName Field name for error messages ("dateOut" or "dateReturn")
     * @return Formatted date string in dd/MM/yyyy format
     * @throws ValidationException if date format is incorrect
     */
    private String validateAndFormatDate(String dateStr, String fieldName)
    {
        if (dateStr == null || dateStr.isBlank()) {
            throw new ValidationException(
                    String.format("%s is required", fieldName));
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, INPUT_FORMAT);
            String formattedDate = date.format(OUTPUT_FORMAT);
            LOG.debugf("Formatted %s from '%s' to '%s'", fieldName, dateStr, formattedDate);
            return formattedDate;
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    String.format(
                            "Invalid date format for %s: '%s'. Expected format: %s",
                            fieldName,
                            dateStr,
                            EXPECTED_DATE_FORMAT));
        } catch (Exception e) {
            LOG.errorf(e, "Error formatting %s date: %s", fieldName, dateStr);
            throw new ValidationException(
                    String.format(
                            "Error formatting %s date '%s'. Expected format: %s",
                            fieldName,
                            dateStr,
                            EXPECTED_DATE_FORMAT));
        }
    }

    /**
     * Validates required and optional fields for train search
     *
     * @param origin      Station origin (required)
     * @param destination Station destination (required)
     * @param dateOut     Outbound date (required)
     * @param adults      Number of adult passengers (required, must be > 0) as string
     * @throws ValidationException if validation fails
     */
    private void validateRequiredFields(String origin, String destination, String dateOut, String adults)
    {
        if (origin == null || origin.isBlank()) {
            throw new ValidationException("Origin is required");
        }

        if (destination == null || destination.isBlank()) {
            throw new ValidationException("Destination is required");
        }

        if (dateOut == null || dateOut.isBlank()) {
            throw new ValidationException("Date out is required");
        }

        if (adults == null || adults.isBlank()) {
            throw new ValidationException("Adults is required");
        }

        try {
            int adultsInt = Integer.parseInt(adults.trim());
            if (adultsInt <= 0) {
                throw new ValidationException("Adults must be greater than 0");
            }
            if (adultsInt > 8) {
                throw new ValidationException("Adults must be at most 8");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Adults must be a valid number");
        }
    }

    /**
     * Validates that origin and destination stations exist and are unique
     * Returns the station data needed for form submission
     *
     * @param origin      Station origin name (search text)
     * @param destination Station destination name (search text)
     * @return StationValidationResult with station data (name, desgEstacion, clave)
     * @throws ValidationException if validation fails
     */
    private StationValidationResult validateStations(String origin, String destination)
    {
        StationData originData = validateStation(origin, "origin");
        StationData destinationData = validateStation(destination, "destination");
        return new StationValidationResult(originData, destinationData);
    }

    /**
     * Validates that a station exists and is unique
     * Returns the station data needed for form submission
     *
     * @param stationName Station name to validate (search text)
     * @param fieldName   Field name for error messages ("origin" or "destination")
     * @return StationData with stationNamePlano, desgEstacion, and clave
     * @throws ValidationException if validation fails
     */
    private StationData validateStation(String stationName, String fieldName)
    {
        try {
            List<Station> matchingStations = getStationsUseCase.searchStations(stationName);

            if (matchingStations.isEmpty()) {
                throw new ValidationException(
                        String.format("No station found matching '%s' for %s", stationName, fieldName));
            }

            if (matchingStations.size() > 1) {
                List<String> stationNames = matchingStations.stream()
                        .map(Station::getStationNamePlano)
                        .filter(name -> name != null && !name.isBlank())
                        .collect(Collectors.toList());

                String stationNamesList = String.join(", ", stationNames);
                throw new ValidationException(
                        String.format(
                                "Please provide a more precise station name. The current search for %s matches the following stations: [%s]",
                                fieldName,
                                stationNamesList));
            }

            // Exactly one station found - extract all needed data
            Station foundStation = matchingStations.get(0);
            String realStationName = foundStation.getStationNamePlano();
            if (realStationName == null || realStationName.isBlank()) {
                // Fallback to stationName if stationNamePlano is null/blank
                realStationName = foundStation.getStationName();
                if (realStationName == null || realStationName.isBlank()) {
                    // Final fallback to search text
                    realStationName = stationName;
                }
            }

            String desgEstacion = foundStation.getStationName() != null && !foundStation.getStationName().isBlank()
                    ? foundStation.getStationName()
                    : realStationName;
            String clave = foundStation.getKey() != null && !foundStation.getKey().isBlank()
                    ? foundStation.getKey()
                    : "";

            LOG.debugf("Validated %s station: %s (desgEstacion: %s, clave: %s, search text: %s)",
                    fieldName, realStationName, desgEstacion, clave, stationName);
            return new StationData(realStationName, desgEstacion, clave);

        } catch (ValidationException e) {
            // Re-throw validation exceptions as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Error validating %s station: %s", fieldName, e.getMessage());
            throw new ValidationException(
                    String.format("Error validating %s station: %s", fieldName, e.getMessage()));
        }
    }

    /**
     * Station data needed for form submission
     */
    private static class StationData
    {
        private final String stationNamePlano;
        private final String desgEstacion;
        private final String clave;

        StationData(String stationNamePlano, String desgEstacion, String clave)
        {
            this.stationNamePlano = stationNamePlano;
            this.desgEstacion = desgEstacion;
            this.clave = clave;
        }

        String getStationNamePlano()
        {
            return stationNamePlano;
        }

        String getDesgEstacion()
        {
            return desgEstacion;
        }

        String getClave()
        {
            return clave;
        }
    }

    /**
     * Result of station validation containing the station data to use
     */
    private static class StationValidationResult
    {
        private final StationData originData;
        private final StationData destinationData;

        StationValidationResult(StationData originData, StationData destinationData)
        {
            this.originData = originData;
            this.destinationData = destinationData;
        }

        String getOriginStationName()
        {
            return originData.getStationNamePlano();
        }

        String getDestinationStationName()
        {
            return destinationData.getStationNamePlano();
        }

        String getOriginDesgEstacion()
        {
            return originData.getDesgEstacion();
        }

        String getDestinationDesgEstacion()
        {
            return destinationData.getDesgEstacion();
        }

        String getOriginClave()
        {
            return originData.getClave();
        }

        String getDestinationClave()
        {
            return destinationData.getClave();
        }
    }
}
