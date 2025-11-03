package com.delard.renfe.navigation.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for handling Renfe station data and date conversions
 * Translated from Python renfe_common.py
 */
@ApplicationScoped
public class RenfeCommonService {

    private static final Logger LOG = Logger.getLogger(RenfeCommonService.class);
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Map<String, String>> stations;

    /**
     * Load station catalog from JSON resource
     */
    private void loadStations() {
        if (stations != null) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream resourceStream = getClass().getResourceAsStream("/estaciones.json");

            if (resourceStream == null) {
                LOG.warn("[SCRAPER] estaciones.json not found in resources");
                stations = Collections.emptyList();
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> loadedStations = mapper.readValue(resourceStream, List.class);
            stations = loadedStations;
            LOG.infof("[SCRAPER] Loaded %d stations from catalog", stations.size());

        } catch (Exception e) {
            LOG.warnf(e, "[SCRAPER] Could not load estaciones.json");
            stations = Collections.emptyList();
        }
    }

    /**
     * Find a station by name in the catalog
     *
     * @param stationName Name of the station to search for
     * @return Map with station data (cdgoEstacion, desgEstacion, clave, etc)
     */
    public Map<String, String> findStation(String stationName) {
        loadStations();

        String stationUpper = stationName.toUpperCase();

        // Exact match first
        for (Map<String, String> station : stations) {
            String desgPlano = station.getOrDefault("desgEstacionPlano", "").toUpperCase();
            String cdgoEst = station.getOrDefault("cdgoEstacion", "").toUpperCase();

            if (desgPlano.equals(stationUpper) || cdgoEst.equals(stationUpper)) {
                return station;
            }
        }

        // Partial match
        for (Map<String, String> station : stations) {
            String plano = station.getOrDefault("desgEstacionPlano", "").toUpperCase();
            if (stationUpper.contains(plano) || plano.startsWith(stationUpper)) {
                return station;
            }
        }

        // If not found, return generic data
        LOG.warnf("[SCRAPER] Station '%s' not found in catalog, using generic search", stationName);
        Map<String, String> genericStation = new HashMap<>();
        genericStation.put("cdgoEstacion", stationName.toUpperCase().substring(0, Math.min(5, stationName.length())));
        genericStation.put("cdgoAdmon", "0071");
        genericStation.put("desgEstacion", stationName.toUpperCase());
        genericStation.put("clave", "0071," + stationName.toUpperCase().substring(0, Math.min(5, stationName.length())) + ",null");

        return genericStation;
    }

    /**
     * Convert date from YYYY-MM-DD format to DD/MM/YYYY format
     *
     * @param dateStr Date string in YYYY-MM-DD format
     * @return Date string in DD/MM/YYYY format
     */
    public String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, INPUT_FORMAT);
            return date.format(OUTPUT_FORMAT);
        } catch (Exception e) {
            LOG.warnf(e, "[SCRAPER] Error formatting date: %s", dateStr);
            return dateStr;
        }
    }
}

