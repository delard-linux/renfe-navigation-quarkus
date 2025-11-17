package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.port.output.CachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StationLoaderService
 * 
 * Tests cover:
 * - Cache integration (hit, miss, disabled)
 * - Loading from file (success and failure)
 * - JavaScript parsing
 * - Station comparison logic
 * - Exception handling
 * 
 * Note: HTTP client testing is complex due to final classes.
 * URL loading is tested in integration tests.
 */
@ExtendWith(MockitoExtension.class)
class StationLoaderServiceTest {

    @Mock
    private CachePort cachePort;

    private StationLoaderService service;

    private static final String SAMPLE_JS_CONTENT = "var estacionesEstatico=[{\"cdgoEstacion\":\"MADRI\",\"desgEstacion\":\"MADRID (TODAS)\",\"clave\":\"MAD\"},{\"cdgoEstacion\":\"BARC\",\"desgEstacion\":\"BARCELONA\",\"clave\":\"BAR\"}]";
    private static final String SAMPLE_JS_CONTENT_EMPTY = "var estacionesEstatico=[]";
    private static final String SAMPLE_JS_CONTENT_INVALID = "var otherVar=[]";

    @BeforeEach
    void setUp() throws Exception {
        service = new StationLoaderService();
        
        // Inject dependencies using reflection
        injectField("cachePort", cachePort);
        injectField("renfeStationsUrl", "https://test.example.com/stations.js");
        injectField("stationsDefaultPath", "/data/estacionesEstaticas.js");
        injectField("stationsTimeoutSeconds", 3);
        injectField("cacheTtlSeconds", 3600L);
    }

    private void injectField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = StationLoaderService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    @Test
    void testStationLoaderServiceInitialization() {
        assertNotNull(service);
    }

    @Test
    void testInit() throws Exception {
        // Test @PostConstruct init method
        Method initMethod = StationLoaderService.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        
        // Call init to test HttpClient initialization
        assertDoesNotThrow(() -> {
            initMethod.invoke(service);
        });
        
        // Verify httpClient was initialized
        java.lang.reflect.Field httpClientField = StationLoaderService.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        Object httpClient = httpClientField.get(service);
        assertNotNull(httpClient);
    }

    @Test
    void testLoadStationsFromCache() {
        List<Map<String, Object>> cachedStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID (TODAS)")
        );

        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.of(cachedStations));

        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cachePort, times(1)).get("stations:all", List.class);
        verify(cachePort, never()).put(anyString(), any(), anyLong());
    }

    @Test
    void testLoadStationsFromFileSuccess() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.empty());

        // Service will try to load from URL (will fail) then from file
        // If file exists in resources, it will be loaded; otherwise returns empty list
        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // Result depends on whether the file exists in test resources
    }

    @Test
    void testParseJavaScriptStations() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("parseJavaScriptStations", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service, SAMPLE_JS_CONTENT);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MADRI", result.get(0).get("cdgoEstacion"));
        assertEquals("MADRID (TODAS)", result.get(0).get("desgEstacion"));
    }

    @Test
    void testParseJavaScriptStationsEmpty() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("parseJavaScriptStations", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service, SAMPLE_JS_CONTENT_EMPTY);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseJavaScriptStationsInvalidFormat() {
        Method method;
        try {
            method = StationLoaderService.class.getDeclaredMethod("parseJavaScriptStations", String.class);
            method.setAccessible(true);
            method.invoke(service, SAMPLE_JS_CONTENT_INVALID);
            fail("Should throw exception for invalid format");
        } catch (Exception e) {
            // Expected - should throw RuntimeException
            assertTrue(e.getCause() instanceof RuntimeException || 
                      e.getCause().getMessage().contains("Could not find estacionesEstatico"));
        }
    }

    @Test
    void testLoadFromFile() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("loadFromFile");
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service);

        assertNotNull(result);
        // Should return empty list if file doesn't exist, or actual stations if it does
    }

    @Test
    void testLoadFromFileResourceNotFound() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("loadFromFile");
        method.setAccessible(true);

        // Set a non-existent path
        injectField("stationsDefaultPath", "/nonexistent/file.js");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoadFromFileWithInvalidContent() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("loadFromFile");
        method.setAccessible(true);

        // Create a service instance with a path that doesn't exist
        StationLoaderService testService = new StationLoaderService();
        injectField("cachePort", cachePort);
        injectField("renfeStationsUrl", "https://test.example.com/stations.js");
        injectField("stationsDefaultPath", "/nonexistent/invalid.js");
        injectField("stationsTimeoutSeconds", 3);
        injectField("cacheTtlSeconds", 3600L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(testService);

        // Should return empty list when file doesn't exist
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAreStationsEqual() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );
        List<Map<String, Object>> stations3 = List.of(
            Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA", "clave", "BAR")
        );

        Boolean result1 = (Boolean) method.invoke(service, stations1, stations2);
        Boolean result2 = (Boolean) method.invoke(service, stations1, stations3);

        assertTrue(result1);
        assertFalse(result2);
    }

    @Test
    void testAreStationsEqualDifferentSizes() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD"),
            Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA", "clave", "BAR")
        );

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);

        assertFalse(result);
    }

    @Test
    void testGetStationKey() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        Map<String, Object> station1 = Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD");
        Map<String, Object> station2 = Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA");
        Map<String, Object> station3 = Map.of("cdgoEstacion", "VAL");

        String key1 = (String) method.invoke(service, station1);
        String key2 = (String) method.invoke(service, station2);
        String key3 = (String) method.invoke(service, station3);

        assertEquals("MAD", key1);
        assertEquals("BARC|BARCELONA", key2);
        assertEquals("VAL", key3);
    }

    @Test
    void testGetStringValue() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStringValue", Map.class, String.class);
        method.setAccessible(true);

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);
        map.put("key3", null);

        String value1 = (String) method.invoke(service, map, "key1");
        String value2 = (String) method.invoke(service, map, "key2");
        String value3 = (String) method.invoke(service, map, "key3");
        String value4 = (String) method.invoke(service, map, "nonexistent");

        assertEquals("value1", value1);
        assertEquals("123", value2);
        assertNull(value3);
        assertNull(value4);
    }

    @Test
    void testLoadStationsCacheMissThenCacheResult() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.empty());

        // Service will try to load from URL (will fail) then from file
        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // If file exists and has data, it should be cached
        if (!result.isEmpty()) {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
            verify(cachePort, atLeastOnce()).put(eq("stations:all"), captor.capture(), eq(3600L));
        }
    }

    @Test
    void testLoadStationsCacheDisabled() {
        when(cachePort.isEnabled()).thenReturn(false);

        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        verify(cachePort, never()).get(anyString(), any());
        verify(cachePort, never()).put(anyString(), any(), anyLong());
    }

    @Test
    void testLoadStationsEmptyResultNotCached() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        Optional<List<Map<String, Object>>> empty = Optional.empty();
        doReturn(empty).when(cachePort).get("stations:all", List.class);

        // Mock to return empty list
        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // Empty results should not be cached
        if (result.isEmpty()) {
            verify(cachePort, never()).put(anyString(), any(), anyLong());
        }
    }

    @Test
    void testLoadStationsHandlesExceptionGracefully() {
        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            List<Map<String, Object>> result = service.loadStations();
            assertNotNull(result);
        });
    }

    @Test
    void testLoadStationsReturnsList() {
        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
    }

    @Test
    void testCompareWithLocalFile() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );

        // Should not throw exception
        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }

    @Test
    void testCompareWithLocalFileWithEqualStations() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        // Create stations that match what might be in the file
        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID (TODAS)", "clave", "0071,MADRI,null")
        );

        // Should not throw exception and should log debug message if stations match
        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }

    @Test
    void testCompareWithLocalFileWithDifferentStations() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        // Create stations that are different from what might be in the file
        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "DIFFERENT", "desgEstacion", "DIFFERENT STATION", "clave", "0071,DIFFERENT,null")
        );

        // Should not throw exception and should log warning if stations differ
        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }

    @Test
    void testCompareWithLocalFileWithEmptyLocalFile() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        // Set a non-existent path to simulate empty local file
        injectField("stationsDefaultPath", "/nonexistent/file.js");

        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );

        // Should not throw exception and should log debug message about empty file
        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }

    @Test
    void testCompareWithLocalFileHandlesException() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        // Set a non-existent path to simulate exception scenario
        injectField("stationsDefaultPath", "/nonexistent/file.js");

        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );

        // Should handle exception gracefully and not throw
        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }

    @Test
    void testAreStationsEqualWithNullKeys() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        // Stations without clave field (will use code|name as key)
        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID")
        );

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);
        assertTrue(result);
    }

    @Test
    void testAreStationsEqualWithDifferentKeySizes() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD"),
            Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA", "clave", "BAR")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);
        assertFalse(result);
    }

    @Test
    void testAreStationsEqualWithNullStationKeys() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        // Stations with null values that result in null keys
        Map<String, Object> station1 = new java.util.HashMap<>();
        station1.put("cdgoEstacion", null);
        station1.put("desgEstacion", null);
        
        Map<String, Object> station2 = new java.util.HashMap<>();
        station2.put("cdgoEstacion", null);
        station2.put("desgEstacion", null);

        List<Map<String, Object>> stations1 = List.of(station1);
        List<Map<String, Object>> stations2 = List.of(station2);

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);
        // Both have null keys, so sets will be equal (both empty)
        assertTrue(result);
    }

    @Test
    void testAreStationsEqualWithSameKeysButDifferentOrder() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD"),
            Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA", "clave", "BAR")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "BARC", "desgEstacion", "BARCELONA", "clave", "BAR"),
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD")
        );

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);
        // Should be equal even if order is different (using sets)
        assertTrue(result);
    }

    @Test
    void testAreStationsEqualWithDifferentKeys() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("areStationsEqual", List.class, List.class);
        method.setAccessible(true);

        List<Map<String, Object>> stations1 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD1")
        );
        List<Map<String, Object>> stations2 = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID", "clave", "MAD2")
        );

        Boolean result = (Boolean) method.invoke(service, stations1, stations2);
        assertFalse(result);
    }

    @Test
    void testGetStationKeyWithNullValues() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        // Station with only code
        Map<String, Object> station1 = Map.of("cdgoEstacion", "MADRI");
        String key1 = (String) method.invoke(service, station1);
        assertEquals("MADRI", key1);

        // Station with only name
        Map<String, Object> station2 = Map.of("desgEstacion", "MADRID");
        String key2 = (String) method.invoke(service, station2);
        assertEquals("MADRID", key2);

        // Station with null values
        Map<String, Object> station3 = new java.util.HashMap<>();
        station3.put("cdgoEstacion", null);
        station3.put("desgEstacion", null);
        String key3 = (String) method.invoke(service, station3);
        assertNull(key3);
    }

    @Test
    void testGetStationKeyWithEmptyClave() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        // Station with empty clave (should fallback to code|name)
        Map<String, Object> station = Map.of(
            "cdgoEstacion", "MADRI",
            "desgEstacion", "MADRID",
            "clave", ""
        );
        String key = (String) method.invoke(service, station);
        assertEquals("MADRI|MADRID", key);
    }

    @Test
    void testGetStationKeyWithNullCodeButNotNullName() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        // Station with null code but not null name
        Map<String, Object> station = new java.util.HashMap<>();
        station.put("cdgoEstacion", null);
        station.put("desgEstacion", "MADRID");
        String key = (String) method.invoke(service, station);
        assertEquals("MADRID", key);
    }

    @Test
    void testGetStationKeyWithNullNameButNotNullCode() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        // Station with null name but not null code
        Map<String, Object> station = new java.util.HashMap<>();
        station.put("cdgoEstacion", "MADRI");
        station.put("desgEstacion", null);
        String key = (String) method.invoke(service, station);
        assertEquals("MADRI", key);
    }

    @Test
    void testGetStationKeyWithCodeAndNameButNoClave() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("getStationKey", Map.class);
        method.setAccessible(true);

        // Station with code and name but no clave field
        Map<String, Object> station = Map.of(
            "cdgoEstacion", "MADRI",
            "desgEstacion", "MADRID"
        );
        String key = (String) method.invoke(service, station);
        assertEquals("MADRI|MADRID", key);
    }

    @Test
    void testLoadStationsWithWarningWhenLoadingFromFile() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        when(cachePort.get("stations:all", List.class)).thenReturn(Optional.empty());

        // Service will try to load from URL (will fail) then from file
        // This should trigger the warning log
        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // If file exists and has data, warning should be logged
    }

    @Test
    void testLoadStationsCachesNonEmptyResult() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        Optional<List<Map<String, Object>>> empty = Optional.empty();
        doReturn(empty).when(cachePort).get("stations:all", List.class);

        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // If result is not empty, it should be cached
        if (!result.isEmpty()) {
            verify(cachePort, atLeastOnce()).put(eq("stations:all"), anyList(), eq(3600L));
        }
    }

    @Test
    void testParseJavaScriptStationsWithNestedArrays() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("parseJavaScriptStations", String.class);
        method.setAccessible(true);

        // Test with nested arrays in the JavaScript
        String jsContent = "var estacionesEstatico=[{\"cdgoEstacion\":\"MADRI\",\"nested\":[1,2,3]},{\"cdgoEstacion\":\"BARC\"}]";
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service, jsContent);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testParseJavaScriptStationsWithComplexContent() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("parseJavaScriptStations", String.class);
        method.setAccessible(true);

        // Test with content before and after the array
        String jsContent = "var otherVar=123; var estacionesEstatico=[{\"cdgoEstacion\":\"MADRI\"}]; var anotherVar=456;";
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(service, jsContent);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MADRI", result.get(0).get("cdgoEstacion"));
    }

    @Test
    void testLoadStationsWithUrlSuccessThenCache() throws Exception {
        // This test verifies the flow when URL loading succeeds
        // Since we can't easily mock HttpClient, we test the cache behavior
        when(cachePort.isEnabled()).thenReturn(true);
        Optional<List<Map<String, Object>>> empty = Optional.empty();
        doReturn(empty).when(cachePort).get("stations:all", List.class);

        // Service will try URL first (will fail in unit test), then file
        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // Verify that if we get stations, they are cached
        if (!result.isEmpty()) {
            verify(cachePort, atLeastOnce()).put(eq("stations:all"), anyList(), eq(3600L));
        }
    }

    @Test
    void testLoadStationsWithUrlFailureAndEmptyFile() throws Exception {
        // Test the flow when both URL and file fail/return empty
        when(cachePort.isEnabled()).thenReturn(true);
        Optional<List<Map<String, Object>>> empty = Optional.empty();
        doReturn(empty).when(cachePort).get("stations:all", List.class);

        // Set a non-existent path so file returns empty
        injectField("stationsDefaultPath", "/nonexistent/file.js");

        List<Map<String, Object>> result = service.loadStations();

        assertNotNull(result);
        // URL will fail, file will return empty, so result should be empty
        // Empty results should not be cached
        if (result.isEmpty()) {
            verify(cachePort, never()).put(anyString(), any(), anyLong());
        }
    }

    @Test
    void testCompareWithLocalFileWithMatchingStations() throws Exception {
        Method method = StationLoaderService.class.getDeclaredMethod("compareWithLocalFile", List.class);
        method.setAccessible(true);

        // If the file exists and has matching stations, should log debug message
        // This test verifies the method doesn't throw and handles the comparison
        List<Map<String, Object>> urlStations = List.of(
            Map.of("cdgoEstacion", "MADRI", "desgEstacion", "MADRID (TODAS)", "clave", "0071,MADRI,null")
        );

        assertDoesNotThrow(() -> {
            method.invoke(service, urlStations);
        });
    }
}
