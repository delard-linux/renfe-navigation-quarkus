package com.delard.renfe.navigation.infrastructure.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StationLoaderService
 * 
 * Note: Testing HTTP client interactions is complex and typically done in integration tests.
 * These unit tests focus on verifying the service structure and exception handling.
 * Full URL loading and JavaScript parsing are tested in integration tests.
 */
class StationLoaderServiceTest {

    @Test
    void testStationLoaderServiceInitialization() {
        StationLoaderService service = new StationLoaderService();
        assertNotNull(service);
    }

    @Test
    void testLoadStationsHandlesExceptionGracefully() {
        // The service should handle exceptions and fallback to file
        // This test verifies the service doesn't throw unhandled exceptions
        StationLoaderService service = new StationLoaderService();
        
        // When URL fails, it should fallback to file
        // If file also fails, it should return empty list instead of throwing
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> result = service.loadStations();
            // Result can be empty if both URL and file fail, but should not throw
            assertNotNull(result);
        });
    }

    @Test
    void testLoadStationsReturnsList() {
        StationLoaderService service = new StationLoaderService();
        
        List<Map<String, Object>> result = service.loadStations();
        
        // Should return a list (may be empty if both sources fail)
        assertNotNull(result);
    }
}

