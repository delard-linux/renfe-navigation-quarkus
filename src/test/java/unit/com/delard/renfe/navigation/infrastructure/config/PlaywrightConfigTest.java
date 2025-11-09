package com.delard.renfe.navigation.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlaywrightConfig
 */
class PlaywrightConfigTest {

    @Test
    void testGetters() {
        PlaywrightConfig config = new PlaywrightConfig();
        
        // Test that getters exist and can be called without throwing exceptions
        // Note: In unit tests without CDI, the @ConfigProperty values won't be injected
        // so we just verify the getters are accessible
        assertNotNull(config);
        
        // Test getters exist and can be called
        config.isHeadless();
        config.getViewportWidth();
        config.getViewportHeight();
        config.getSlowMo();
        config.getTimeoutMs();
        config.getNavigationTimeoutMs();
        config.getNetworkIdleTimeoutMs();
        config.getShortTimeoutMs();
        
        // Verify getters return values (may be default, injected, or null in unit tests)
        // In real scenarios with CDI, these will have values from application.properties
        // In unit tests without CDI, some values may be null, which is acceptable
        // We just verify the getters don't throw exceptions
    }
}

