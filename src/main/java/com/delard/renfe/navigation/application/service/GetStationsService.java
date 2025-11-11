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
     *
     * @param searchText Search text to validate
     * @throws ValidationException if validation fails
     */
    private void validateSearchText(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            throw new ValidationException("Search text is required");
        }

        if (searchText.trim().length() < 3) {
            throw new ValidationException("Search text must have at least 3 characters");
        }
    }
}

