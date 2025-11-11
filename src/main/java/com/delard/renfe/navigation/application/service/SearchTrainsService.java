package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsUseCase;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for searching trains
 */
@ApplicationScoped
public class SearchTrainsService implements SearchTrainsUseCase {

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
                                       String dateReturn, int adults) {
        Instant startTime = Instant.now();

        // Validate required parameters
        validateRequiredFields(origin, destination, dateOut, adults);

        // Validate and format dates
        String formattedDateOut = validateAndFormatDate(dateOut, "dateOut");
        String formattedDateReturn = dateReturn != null && !dateReturn.isBlank() 
                ? validateAndFormatDate(dateReturn, "dateReturn") 
                : null;

        // Validate stations exist and are unique, get real station names
        StationValidationResult stationValidation = validateStations(origin, destination);
        String realOrigin = stationValidation.getOriginStationName();
        String realDestination = stationValidation.getDestinationStationName();

        LOG.debugf("[REQUEST] Starting search: %s -> %s, Outbound: %s, Return: %s, Passengers: %d",
                realOrigin, realDestination, formattedDateOut, formattedDateReturn, adults);

        try {
            List<List<Train>> result = trainScraperPort.scrapeTrains(
                    realOrigin, realDestination, formattedDateOut, formattedDateReturn, adults);

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
    private String validateAndFormatDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new ValidationException(
                String.format("%s is required", fieldName)
            );
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
                    EXPECTED_DATE_FORMAT
                )
            );
        } catch (Exception e) {
            LOG.errorf(e, "Error formatting %s date: %s", fieldName, dateStr);
            throw new ValidationException(
                String.format(
                    "Error formatting %s date '%s'. Expected format: %s",
                    fieldName,
                    dateStr,
                    EXPECTED_DATE_FORMAT
                )
            );
        }
    }

    /**
     * Validates required and optional fields for train search
     *
     * @param origin      Station origin (required)
     * @param destination Station destination (required)
     * @param dateOut     Outbound date (required)
     * @param adults      Number of adult passengers (required, must be > 0)
     * @throws ValidationException if validation fails
     */
    private void validateRequiredFields(String origin, String destination, String dateOut, int adults) {
        if (origin == null || origin.isBlank()) {
            throw new ValidationException("Origin is required");
        }

        if (destination == null || destination.isBlank()) {
            throw new ValidationException("Destination is required");
        }

        if (dateOut == null || dateOut.isBlank()) {
            throw new ValidationException("Date out is required");
        }

        if (adults <= 0) {
            throw new ValidationException("Adults must be greater than 0");
        }
    }

    /**
     * Validates that origin and destination stations exist and are unique
     * Returns the real station names (desgEstacionPlano) to use in the search
     *
     * @param origin      Station origin name (search text)
     * @param destination Station destination name (search text)
     * @return StationValidationResult with real station names
     * @throws ValidationException if validation fails
     */
    private StationValidationResult validateStations(String origin, String destination) {
        String realOrigin = validateStation(origin, "origin");
        String realDestination = validateStation(destination, "destination");
        return new StationValidationResult(realOrigin, realDestination);
    }

    /**
     * Validates that a station exists and is unique
     * Returns the real station name (desgEstacionPlano) to use in the search
     *
     * @param stationName Station name to validate (search text)
     * @param fieldName   Field name for error messages ("origin" or "destination")
     * @return Real station name (desgEstacionPlano) to use in the search
     * @throws ValidationException if validation fails
     */
    private String validateStation(String stationName, String fieldName) {
        try {
            List<Station> matchingStations = getStationsUseCase.searchStations(stationName);

            if (matchingStations.isEmpty()) {
                throw new ValidationException(
                    String.format("No station found matching '%s' for %s", stationName, fieldName)
                );
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
                        stationNamesList
                    )
                );
            }

            // Exactly one station found - return the real station name (desgEstacionPlano)
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
            LOG.debugf("Validated %s station: %s (search text: %s)", fieldName, realStationName, stationName);
            return realStationName;

        } catch (ValidationException e) {
            // Re-throw validation exceptions as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Error validating %s station: %s", fieldName, e.getMessage());
            throw new ValidationException(
                String.format("Error validating %s station: %s", fieldName, e.getMessage())
            );
        }
    }

    /**
     * Result of station validation containing the real station names to use
     */
    private static class StationValidationResult {
        private final String originStationName;
        private final String destinationStationName;

        StationValidationResult(String originStationName, String destinationStationName) {
            this.originStationName = originStationName;
            this.destinationStationName = destinationStationName;
        }

        String getOriginStationName() {
            return originStationName;
        }

        String getDestinationStationName() {
            return destinationStationName;
        }
    }
}

