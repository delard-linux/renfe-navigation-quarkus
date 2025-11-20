package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;
import com.delard.renfe.navigation.domain.port.output.StationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Application service for getting stations
 */
@ApplicationScoped
public class GetStationsService implements GetStationsUseCase {

    private static final Logger LOG = Logger.getLogger(GetStationsService.class);

    @Inject
    StationRepository stationRepository;

    @Override
    public List<Station> getAllStations() {
        LOG.debug("[REQUEST] Getting all stations");
        
        try {
            List<Station> stations = stationRepository.loadAllStations();
            if (stations == null) {
                LOG.warn("[WARN] Repository returned null, returning empty list");
                return List.of();
            }
            LOG.debugf("[SUCCESS] Loaded %d stations", stations.size());
            return stations;
        } catch (Exception e) {
            LOG.errorf(e, "[ERROR] Failed to load stations: %s", e.getMessage());
            throw new RuntimeException("Error loading stations: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Station> searchStations(String searchText) {
        LOG.debugf("[REQUEST] Searching stations with text: %s", searchText);
        
        // Validate search text
        validateSearchText(searchText);
        
        try {
            List<Station> stations = stationRepository.searchStations(searchText);
            if (stations == null) {
                LOG.warn("[WARN] Repository returned null, returning empty list");
                return List.of();
            }
            LOG.debugf("[SUCCESS] Found %d stations matching '%s'", stations.size(), searchText);
            return stations;
        } catch (ValidationException e) {
            // Re-throw validation exceptions as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "[ERROR] Failed to search stations: %s", e.getMessage());
            throw new RuntimeException("Error searching stations: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the search text parameter
     * The search text can be a set of words separated by spaces.
     * If all words have 3 or fewer characters, then all words must have at least 3 characters.
     * If there is at least one word with more than 3 characters, the validation per word is skipped
     * (allowing short words like "DE" when combined with longer words like "MADRID").
     *
     * @param searchText Search text to validate
     * @throws ValidationException if validation fails
     */
    private void validateSearchText(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            throw new ValidationException("Search text is required");
        }

        String trimmedText = searchText.trim();
        if (trimmedText.length() < 3) {
            throw new ValidationException("Search text must have at least 3 characters");
        }

        // Split by spaces and check if there's any word with more than 3 characters
        String[] words = trimmedText.split("\\s+");
        boolean hasLongWord = false;
        for (String word : words) {
            if (word.length() > 3) {
                hasLongWord = true;
                break;
            }
        }

        // Only validate minimum length per word if all words have 3 or fewer characters
        if (!hasLongWord) {
            for (String word : words) {
                if (word.length() < 3) {
                    throw new ValidationException(
                        String.format("Each word in the search text must have at least 3 characters. Found word with %d characters: '%s'", 
                            word.length(), word));
                }
            }
        }
    }
}

