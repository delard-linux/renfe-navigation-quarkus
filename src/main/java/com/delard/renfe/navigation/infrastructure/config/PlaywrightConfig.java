package com.delard.renfe.navigation.infrastructure.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Configuration for Playwright browser automation
 */
@ApplicationScoped
public class PlaywrightConfig {

    private static final Logger LOG = Logger.getLogger(PlaywrightConfig.class);

    // Default values match production configuration in application.properties
    // These values are optimized for production (headless=true, slow-mo=0, etc.)
    // Test profiles can override these values via application-integration.properties
    
    @ConfigProperty(name = "playwright.headless", defaultValue = "true")
    public boolean headless;

    @ConfigProperty(name = "playwright.viewport-width", defaultValue = "1920")
    public int viewportWidth;

    @ConfigProperty(name = "playwright.viewport-height", defaultValue = "1080")
    public int viewportHeight;

    @ConfigProperty(name = "playwright.slow-mo", defaultValue = "0")
    public int slowMo;

    @ConfigProperty(name = "playwright.locale", defaultValue = "es-ES")
    public String locale;

    @ConfigProperty(name = "playwright.timeout-ms", defaultValue = "30000")
    public int timeoutMs;

    @ConfigProperty(name = "playwright.timeout-navigation-ms", defaultValue = "30000")
    public int navigationTimeoutMs;

    @ConfigProperty(name = "playwright.timeout-networkidle-ms", defaultValue = "30000")
    public int networkIdleTimeoutMs;

    @ConfigProperty(name = "playwright.timeout-short-ms", defaultValue = "500")
    public int shortTimeoutMs;

    @ConfigProperty(name = "renfe.search-url")
    public String renfeSearchUrl;

    @ConfigProperty(name = "renfe.responses-dir")
    public String responsesDir;

    @PostConstruct
    void init() {
        LOG.infof("PlaywrightConfig initialized - headless: %s, slow-mo: %d, viewport: %dx%d", 
                headless, slowMo, viewportWidth, viewportHeight);
    }

    public boolean isHeadless() {
        return headless;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public int getSlowMo() {
        return slowMo;
    }

    public String getLocale() {
        return locale;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getNavigationTimeoutMs() {
        return navigationTimeoutMs;
    }

    public int getNetworkIdleTimeoutMs() {
        return networkIdleTimeoutMs;
    }

    public int getShortTimeoutMs() {
        return shortTimeoutMs;
    }

    public String getRenfeSearchUrl() {
        return renfeSearchUrl;
    }

    public String getResponsesDir() {
        return responsesDir;
    }
}
