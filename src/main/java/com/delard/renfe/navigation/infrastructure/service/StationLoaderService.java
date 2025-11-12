package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.port.output.CachePort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for loading station data from URL or file
 */
@ApplicationScoped
public class StationLoaderService {

    private static final Logger LOG = Logger.getLogger(StationLoaderService.class);

    @Inject
    @ConfigProperty(name = "renfe.stations-url", defaultValue = "https://www.renfe.com/content/dam/renfe/es/General/buscadores/javascript/estacionesEstaticas.js")
    String renfeStationsUrl;

    @Inject
    @ConfigProperty(name = "renfe.stations-default", defaultValue = "/data/estacionesEstaticas.js")
    String stationsDefaultPath;

    @Inject
    @ConfigProperty(name = "renfe.stations-timeout", defaultValue = "3")
    int stationsTimeoutSeconds;

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-ttl-seconds", defaultValue = "3600")
    long cacheTtlSeconds;

    @Inject
    CachePort cachePort;

    private final ObjectMapper objectMapper;
    private HttpClient httpClient;
    
    private static final String CACHE_KEY_STATIONS = "stations:all";

    public StationLoaderService() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(stationsTimeoutSeconds))
                .build();
    }

    /**
     * Load stations from Renfe URL, fallback to local file if URL fails
     * Results are cached to improve performance
     *
     * @return List of station maps
     */
    public List<Map<String, Object>> loadStations() {
        // Try to get from cache first
        if (cachePort.isEnabled()) {
            @SuppressWarnings("unchecked")
            Optional<List<Map<String, Object>>> cachedStations = 
                (Optional<List<Map<String, Object>>>) (Optional<?>) cachePort.get(CACHE_KEY_STATIONS, List.class);
            if (cachedStations.isPresent()) {
                LOG.debugf("Stations loaded from cache");
                return cachedStations.get();
            }
        }

        // Cache miss or cache disabled, load from source
        List<Map<String, Object>> stations;
        try {
            LOG.debugf("Attempting to load stations from URL: %s", renfeStationsUrl);
            stations = loadFromUrl();
        } catch (Exception e) {
            LOG.warnf(e, "Failed to load stations from URL, falling back to local file: %s", e.getMessage());
            stations = loadFromFile();
            if (!stations.isEmpty()) {
                LOG.warnf(
                    "[WARNING] Stations loaded from local file (%s) instead of URL. " +
                    "This may indicate network issues or URL unavailability. " +
                    "Loaded %d stations from local file.",
                    stationsDefaultPath,
                    stations.size()
                );
            }
        }

        // Cache the result if cache is enabled and we have stations
        if (cachePort.isEnabled() && !stations.isEmpty()) {
            cachePort.put(CACHE_KEY_STATIONS, stations, cacheTtlSeconds);
            LOG.debugf("Stations cached with TTL: %d seconds", cacheTtlSeconds);
        }

        return stations;
    }

    /**
     * Load stations from Renfe URL
     *
     * @return List of station maps
     */
    private List<Map<String, Object>> loadFromUrl() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(renfeStationsUrl))
                .timeout(Duration.ofSeconds(stationsTimeoutSeconds))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error: " + response.statusCode());
        }

        String content = response.body();
        List<Map<String, Object>> urlStations = parseJavaScriptStations(content);
        
        // Compare with local file and warn if different
        compareWithLocalFile(urlStations);
        
        return urlStations;
    }

    /**
     * Parse JavaScript stations array from Renfe response
     *
     * @param content JavaScript content
     * @return List of station maps
     */
    private List<Map<String, Object>> parseJavaScriptStations(String content) throws Exception {
        // Extract the estacionesEstatico array from JavaScript
        // Try different variations: with and without spaces
        int startIndex = content.indexOf("var estacionesEstatico=[");
        if (startIndex == -1) {
            startIndex = content.indexOf("var estacionesEstatico = [");
        }
        if (startIndex == -1) {
            throw new RuntimeException("Could not find estacionesEstatico array in JavaScript");
        }

        // Find the opening bracket after "var estacionesEstatico"
        int bracketStart = content.indexOf('[', startIndex);
        if (bracketStart == -1) {
            throw new RuntimeException("Could not find opening bracket for estacionesEstatico array");
        }

        // Find the matching closing bracket
        int bracketCount = 0;
        int endIndex = bracketStart;

        for (int i = bracketStart; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    endIndex = i + 1;
                    break;
                }
            }
        }

        if (bracketCount != 0) {
            throw new RuntimeException("Could not find matching closing bracket for estacionesEstatico array");
        }

        String jsonArray = content.substring(bracketStart, endIndex);
        
        List<Map<String, Object>> stations = objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {});
        
        LOG.debugf("Parsed %d stations from JavaScript", stations.size());
        return stations;
    }

    /**
     * Load stations from local JavaScript file
     *
     * @return List of station maps
     */
    private List<Map<String, Object>> loadFromFile() {
        try {
            InputStream resourceStream = getClass().getResourceAsStream(stationsDefaultPath);
            if (resourceStream == null) {
                LOG.warnf("Station file not found: %s", stationsDefaultPath);
                return new ArrayList<>();
            }

            String content = new String(resourceStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            List<Map<String, Object>> stations = parseJavaScriptStations(content);
            
            LOG.debugf("Loaded %d stations from local file", stations.size());
            return stations;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to load stations from file: %s", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Compare stations loaded from URL with local file and warn if different
     *
     * @param urlStations Stations loaded from URL
     */
    private void compareWithLocalFile(List<Map<String, Object>> urlStations) {
        try {
            List<Map<String, Object>> localStations = loadFromFile();
            
            if (localStations.isEmpty()) {
                LOG.debug("Local stations file is empty or not found, skipping comparison");
                return;
            }
            
            if (!areStationsEqual(urlStations, localStations)) {
                LOG.warnf(
                    "[WARNING] Local static stations file (%s) differs from URL stations. " +
                    "URL has %d stations, local file has %d stations. " +
                    "Consider updating the local file to match the URL version.",
                    stationsDefaultPath,
                    urlStations.size(),
                    localStations.size()
                );
            } else {
                LOG.debugf("Local stations file matches URL stations (%d stations)", urlStations.size());
            }
        } catch (Exception e) {
            LOG.debugf(e, "Could not compare with local file: %s", e.getMessage());
        }
    }

    /**
     * Check if two station lists are equal
     * Compares by station code, name, and key to determine equality
     *
     * @param stations1 First list of stations
     * @param stations2 Second list of stations
     * @return true if lists are equal, false otherwise
     */
    private boolean areStationsEqual(List<Map<String, Object>> stations1, List<Map<String, Object>> stations2) {
        if (stations1.size() != stations2.size()) {
            return false;
        }
        
        // Create a set of station keys for quick lookup
        java.util.Set<String> keys1 = new java.util.HashSet<>();
        for (Map<String, Object> station : stations1) {
            String key = getStationKey(station);
            if (key != null) {
                keys1.add(key);
            }
        }
        
        java.util.Set<String> keys2 = new java.util.HashSet<>();
        for (Map<String, Object> station : stations2) {
            String key = getStationKey(station);
            if (key != null) {
                keys2.add(key);
            }
        }
        
        // Compare sets
        if (keys1.size() != keys2.size()) {
            return false;
        }
        
        return keys1.equals(keys2);
    }

    /**
     * Get a unique key for a station based on its identifying fields
     *
     * @param station Station map
     * @return Unique key string
     */
    private String getStationKey(Map<String, Object> station) {
        String code = getStringValue(station, "cdgoEstacion");
        String name = getStringValue(station, "desgEstacion");
        String key = getStringValue(station, "clave");
        
        // Use clave if available, otherwise use code + name
        if (key != null && !key.isEmpty()) {
            return key;
        }
        if (code != null && name != null) {
            return code + "|" + name;
        }
        return code != null ? code : name;
    }

    /**
     * Get string value from map, handling null
     *
     * @param map Map to get value from
     * @param key Key to look up
     * @return String value or null
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }
}

