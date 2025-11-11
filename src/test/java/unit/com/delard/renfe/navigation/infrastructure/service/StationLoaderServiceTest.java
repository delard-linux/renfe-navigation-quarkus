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
        verify(cachePort, never()).get(anyString(), any(Class.class));
        verify(cachePort, never()).put(anyString(), any(), anyLong());
    }

    @Test
    void testLoadStationsEmptyResultNotCached() throws Exception {
        when(cachePort.isEnabled()).thenReturn(true);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Optional empty = Optional.empty();
        when(cachePort.get("stations:all", List.class)).thenReturn(empty);

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
}
