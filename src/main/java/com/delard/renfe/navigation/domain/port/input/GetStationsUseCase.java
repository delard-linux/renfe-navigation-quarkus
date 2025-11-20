/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.port.input;


import java.util.List;

import com.delard.renfe.navigation.domain.model.Station;


/**
 * Input port for getting all stations
 */
public interface GetStationsUseCase
{

    /**
     * Get all available stations
     *
     * @return List of all stations
     */
    List<Station> getAllStations();

    /**
     * Search stations by text similarity
     * Searches in stationName (desgEstacion) and stationNamePlano (desgEstacionPlano) fields
     *
     * @param searchText Text to search for (case-insensitive, partial match)
     * @return List of stations matching the search criteria
     */
    List<Station> searchStations(String searchText);
}
