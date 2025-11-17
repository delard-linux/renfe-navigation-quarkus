package com.delard.renfe.navigation.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlaywrightConfig
 */
class PlaywrightConfigTest {

    private PlaywrightConfig config;

    @BeforeEach
    void setUp() {
        config = new PlaywrightConfig();
    }

    @Test
    void testGettersWithDefaultValues() {
        // Test that getters exist and can be called without throwing exceptions
        // Note: In unit tests without CDI, the @ConfigProperty values won't be injected
        // so we just verify the getters are accessible
        assertNotNull(config);
        
        // Test getters exist and can be called
        config.isHeadless();
        config.getViewportWidth();
        config.getViewportHeight();
        config.getSlowMo();
        config.getLocale();
        config.getTimeoutMs();
        config.getNavigationTimeoutMs();
        config.getNetworkIdleTimeoutMs();
        config.getShortTimeoutMs();
        config.getRenfeSearchUrl();
        config.getResponsesDir();
    }

    @Test
    void testIsHeadless() throws Exception {
        setField("headless", true);
        assertTrue(config.isHeadless());

        setField("headless", false);
        assertFalse(config.isHeadless());
    }

    @Test
    void testGetViewportWidth() throws Exception {
        setField("viewportWidth", 1920);
        assertEquals(1920, config.getViewportWidth());

        setField("viewportWidth", 1280);
        assertEquals(1280, config.getViewportWidth());
    }

    @Test
    void testGetViewportHeight() throws Exception {
        setField("viewportHeight", 1080);
        assertEquals(1080, config.getViewportHeight());

        setField("viewportHeight", 720);
        assertEquals(720, config.getViewportHeight());
    }

    @Test
    void testGetSlowMo() throws Exception {
        setField("slowMo", 0);
        assertEquals(0, config.getSlowMo());

        setField("slowMo", 500);
        assertEquals(500, config.getSlowMo());
    }

    @Test
    void testGetLocale() throws Exception {
        setField("locale", "es-ES");
        assertEquals("es-ES", config.getLocale());

        setField("locale", "en-US");
        assertEquals("en-US", config.getLocale());
    }

    @Test
    void testGetTimeoutMs() throws Exception {
        setField("timeoutMs", 30000);
        assertEquals(30000, config.getTimeoutMs());

        setField("timeoutMs", 60000);
        assertEquals(60000, config.getTimeoutMs());
    }

    @Test
    void testGetNavigationTimeoutMs() throws Exception {
        setField("navigationTimeoutMs", 30000);
        assertEquals(30000, config.getNavigationTimeoutMs());

        setField("navigationTimeoutMs", 45000);
        assertEquals(45000, config.getNavigationTimeoutMs());
    }

    @Test
    void testGetNetworkIdleTimeoutMs() throws Exception {
        setField("networkIdleTimeoutMs", 30000);
        assertEquals(30000, config.getNetworkIdleTimeoutMs());

        setField("networkIdleTimeoutMs", 60000);
        assertEquals(60000, config.getNetworkIdleTimeoutMs());
    }

    @Test
    void testGetShortTimeoutMs() throws Exception {
        setField("shortTimeoutMs", 500);
        assertEquals(500, config.getShortTimeoutMs());

        setField("shortTimeoutMs", 1000);
        assertEquals(1000, config.getShortTimeoutMs());
    }

    @Test
    void testGetRenfeSearchUrl() throws Exception {
        setField("renfeSearchUrl", "https://www.renfe.com");
        assertEquals("https://www.renfe.com", config.getRenfeSearchUrl());

        setField("renfeSearchUrl", "https://test.renfe.com");
        assertEquals("https://test.renfe.com", config.getRenfeSearchUrl());
    }

    @Test
    void testGetResponsesDir() throws Exception {
        setField("responsesDir", "/tmp/responses");
        assertEquals("/tmp/responses", config.getResponsesDir());

        setField("responsesDir", "target/responses");
        assertEquals("target/responses", config.getResponsesDir());
    }

    @Test
    void testGetRenfeSearchUrlWithNull() throws Exception {
        setField("renfeSearchUrl", null);
        assertNull(config.getRenfeSearchUrl());
    }

    @Test
    void testGetResponsesDirWithNull() throws Exception {
        setField("responsesDir", null);
        assertNull(config.getResponsesDir());
    }

    @Test
    void testInit() throws Exception {
        // Set some values
        setField("headless", true);
        setField("slowMo", 500);
        setField("viewportWidth", 1920);
        setField("viewportHeight", 1080);

        // Call init() method using reflection
        Method initMethod = PlaywrightConfig.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(config);

        // Verify that init() doesn't throw exceptions and values are still accessible
        assertTrue(config.isHeadless());
        assertEquals(500, config.getSlowMo());
        assertEquals(1920, config.getViewportWidth());
        assertEquals(1080, config.getViewportHeight());
    }

    @Test
    void testAllGettersWithValues() throws Exception {
        // Set all fields with test values
        setField("headless", false);
        setField("viewportWidth", 1280);
        setField("viewportHeight", 720);
        setField("slowMo", 1000);
        setField("locale", "en-GB");
        setField("timeoutMs", 45000);
        setField("navigationTimeoutMs", 60000);
        setField("networkIdleTimeoutMs", 90000);
        setField("shortTimeoutMs", 1000);
        setField("renfeSearchUrl", "https://test.example.com");
        setField("responsesDir", "/test/responses");

        // Verify all getters return the correct values
        assertFalse(config.isHeadless());
        assertEquals(1280, config.getViewportWidth());
        assertEquals(720, config.getViewportHeight());
        assertEquals(1000, config.getSlowMo());
        assertEquals("en-GB", config.getLocale());
        assertEquals(45000, config.getTimeoutMs());
        assertEquals(60000, config.getNavigationTimeoutMs());
        assertEquals(90000, config.getNetworkIdleTimeoutMs());
        assertEquals(1000, config.getShortTimeoutMs());
        assertEquals("https://test.example.com", config.getRenfeSearchUrl());
        assertEquals("/test/responses", config.getResponsesDir());
    }

    /**
     * Helper method to set a public field using reflection
     *
     * @param fieldName Name of the field to set
     * @param value Value to set
     * @throws Exception if reflection fails
     */
    private void setField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = PlaywrightConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(config, value);
    }
}

