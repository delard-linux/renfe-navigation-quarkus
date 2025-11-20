/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.port.output;


import java.util.List;

import com.delard.renfe.navigation.domain.model.Station;


/**
 * Output port for station data access
 */
public interface StationRepository
{

    /**
     * Load all stations from the data source
     *
     * @return List of all stations
     */
    List<Station> loadAllStations();

    /**
     * Search stations by text similarity
     * Searches in stationName (desgEstacion) and stationNamePlano (desgEstacionPlano) fields
     *
     * @param searchText Text to search for (case-insensitive, partial match)
     * @return List of stations matching the search criteria
     */
    List<Station> searchStations(String searchText);
}
