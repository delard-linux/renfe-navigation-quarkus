package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.output.StationRepository;
import com.delard.renfe.navigation.infrastructure.service.StationLoaderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Output adapter for loading stations from Renfe URL or local file
 */
@ApplicationScoped
public class RenfeStationRepository implements StationRepository {

    private static final Logger LOG = Logger.getLogger(RenfeStationRepository.class);

    @Inject
    StationLoaderService stationLoaderService;

    @Override
    public List<Station> loadAllStations() {
        List<Map<String, Object>> stationMaps = stationLoaderService.loadStations();
        return convertToDomainStations(stationMaps);
    }

    @Override
    public List<Station> searchStations(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }

        List<Station> allStations = loadAllStations();
        String searchTextUpper = searchText.toUpperCase().trim();
        
        List<Station> matchingStations = new ArrayList<>();
        
        for (Station station : allStations) {
            if (matchesSearch(station, searchTextUpper)) {
                matchingStations.add(station);
            }
        }
        
        LOG.debugf("Found %d stations matching '%s'", matchingStations.size(), searchText);
        return matchingStations;
    }

    /**
     * Check if a station matches the search text
     * Searches in stationName (desgEstacion) and stationNamePlano (desgEstacionPlano)
     *
     * @param station Station to check
     * @param searchTextUpper Uppercase search text
     * @return true if station matches the search criteria
     */
    private boolean matchesSearch(Station station, String searchTextUpper) {
        String stationName = station.getStationName();
        String stationNamePlano = station.getStationNamePlano();
        
        if (stationName != null && stationName.toUpperCase().contains(searchTextUpper)) {
            return true;
        }
        
        if (stationNamePlano != null && stationNamePlano.toUpperCase().contains(searchTextUpper)) {
            return true;
        }
        
        return false;
    }

    /**
     * Convert map representations to domain Station objects
     *
     * @param stationMaps List of station maps
     * @return List of Station domain objects
     */
    private List<Station> convertToDomainStations(List<Map<String, Object>> stationMaps) {
        List<Station> stations = new ArrayList<>();
        
        for (Map<String, Object> stationMap : stationMaps) {
            try {
                Station station = convertToStation(stationMap);
                stations.add(station);
            } catch (Exception e) {
                LOG.warnf(e, "Failed to convert station map to domain object: %s", stationMap);
            }
        }
        
        return stations;
    }

    /**
     * Convert a single station map to Station domain object
     *
     * @param stationMap Station data map
     * @return Station domain object
     */
    private Station convertToStation(Map<String, Object> stationMap) {
        Station station = new Station();
        
        station.setStationCode(getStringValue(stationMap, "cdgoEstacion"));
        station.setAdministrationCode(getStringValue(stationMap, "cdgoAdmon"));
        station.setPriority(getIntegerValue(stationMap, "nmroPrioridad"));
        station.setDescription(getStringValue(stationMap, "descEstacion"));
        station.setStationName(getStringValue(stationMap, "desgEstacion"));
        station.setUicCode(getStringValue(stationMap, "cdgoUic"));
        station.setKey(getStringValue(stationMap, "clave"));
        station.setStationNamePlano(getStringValue(stationMap, "desgEstacionPlano"));
        
        return station;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

