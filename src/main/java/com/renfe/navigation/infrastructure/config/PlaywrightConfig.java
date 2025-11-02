package com.renfe.navigation.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Configuration for Playwright browser automation
 */
@ApplicationScoped
public class PlaywrightConfig {

    @ConfigProperty(name = "playwright.headless", defaultValue = "false")
    public boolean headless;

    @ConfigProperty(name = "playwright.viewport-width", defaultValue = "1920")
    public int viewportWidth;

    @ConfigProperty(name = "playwright.viewport-height", defaultValue = "1080")
    public int viewportHeight;

    @ConfigProperty(name = "playwright.slow-mo", defaultValue = "2000")
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
