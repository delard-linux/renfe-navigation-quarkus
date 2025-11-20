/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.output;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.output.StationRepository;
import com.delard.renfe.navigation.infrastructure.service.StationLoaderService;

import org.jboss.logging.Logger;


/**
 * Output adapter for loading stations from Renfe URL or local file
 */
@ApplicationScoped
public class RenfeStationRepository implements StationRepository
{

    private static final Logger LOG = Logger.getLogger(RenfeStationRepository.class);

    @Inject
    StationLoaderService stationLoaderService;

    @Override
    public List<Station> loadAllStations()
    {
        List<Map<String, Object>> stationMaps = stationLoaderService.loadStations();
        return convertToDomainStations(stationMaps);
    }

    @Override
    public List<Station> searchStations(String searchText)
    {
        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }

        List<Station> allStations = loadAllStations();
        String searchTextUpper = searchText.toUpperCase().trim();

        // Split search text into words
        String[] words = searchTextUpper.split("\\s+");

        // If single word, use simple search
        if (words.length == 1) {
            List<Station> matchingStations = new ArrayList<>();
            for (Station station : allStations) {
                if (matchesSearch(station, searchTextUpper)) {
                    matchingStations.add(station);
                }
            }
            LOG.debugf("Found %d stations matching '%s'", matchingStations.size(), searchText);
            return matchingStations;
        }

        // Multiple words: try AND search first, then OR if no results
        List<Station> andResults = searchStationsWithAnd(allStations, words);

        if (!andResults.isEmpty()) {
            LOG.debugf("Found %d stations matching all words (AND) for '%s'", andResults.size(), searchText);
            return andResults;
        }

        // If no AND results, try OR search
        List<Station> orResults = searchStationsWithOr(allStations, words);
        LOG.debugf("Found %d stations matching any word (OR) for '%s'", orResults.size(), searchText);
        return orResults;
    }

    /**
     * Check if a station matches the search text
     * Searches in stationName (desgEstacion) and stationNamePlano (desgEstacionPlano)
     *
     * @param station Station to check
     * @param searchTextUpper Uppercase search text
     * @return true if station matches the search criteria
     */
    private boolean matchesSearch(Station station, String searchTextUpper)
    {
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
     * Search stations that contain ALL words (AND logic)
     * A station matches if both stationName and stationNamePlano contain all words
     *
     * @param allStations All available stations
     * @param words Array of search words (uppercase)
     * @return List of stations matching all words
     */
    private List<Station> searchStationsWithAnd(List<Station> allStations, String[] words)
    {
        List<Station> matchingStations = new ArrayList<>();

        for (Station station : allStations) {
            if (matchesAllWords(station, words)) {
                matchingStations.add(station);
            }
        }

        return matchingStations;
    }

    /**
     * Search stations that contain ANY word (OR logic)
     * A station matches if stationName or stationNamePlano contains at least one word
     *
     * @param allStations All available stations
     * @param words Array of search words (uppercase)
     * @return List of stations matching any word
     */
    private List<Station> searchStationsWithOr(List<Station> allStations, String[] words)
    {
        List<Station> matchingStations = new ArrayList<>();

        for (Station station : allStations) {
            if (matchesAnyWord(station, words)) {
                matchingStations.add(station);
            }
        }

        return matchingStations;
    }

    /**
     * Check if a station contains all the search words
     * Checks both stationName and stationNamePlano fields
     *
     * @param station Station to check
     * @param words Array of search words (uppercase)
     * @return true if station contains all words
     */
    private boolean matchesAllWords(Station station, String[] words)
    {
        String stationName = station.getStationName();
        String stationNamePlano = station.getStationNamePlano();

        String stationText = "";
        if (stationName != null) {
            stationText = stationName.toUpperCase() + " ";
        }
        if (stationNamePlano != null) {
            stationText += stationNamePlano.toUpperCase();
        }
        stationText = stationText.trim();

        // Check if all words are present in the combined station text
        for (String word : words) {
            if (!stationText.contains(word)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a station contains any of the search words
     * Checks both stationName and stationNamePlano fields
     *
     * @param station Station to check
     * @param words Array of search words (uppercase)
     * @return true if station contains at least one word
     */
    private boolean matchesAnyWord(Station station, String[] words)
    {
        String stationName = station.getStationName();
        String stationNamePlano = station.getStationNamePlano();

        String stationText = "";
        if (stationName != null) {
            stationText = stationName.toUpperCase() + " ";
        }
        if (stationNamePlano != null) {
            stationText += stationNamePlano.toUpperCase();
        }
        stationText = stationText.trim();

        // Check if any word is present in the combined station text
        for (String word : words) {
            if (stationText.contains(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convert map representations to domain Station objects
     *
     * @param stationMaps List of station maps
     * @return List of Station domain objects
     */
    private List<Station> convertToDomainStations(List<Map<String, Object>> stationMaps)
    {
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
    private Station convertToStation(Map<String, Object> stationMap)
    {
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

    private String getStringValue(Map<String, Object> map, String key)
    {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Integer getIntegerValue(Map<String, Object> map, String key)
    {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
