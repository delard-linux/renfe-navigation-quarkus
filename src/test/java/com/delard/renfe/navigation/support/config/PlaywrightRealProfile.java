package com.delard.renfe.navigation.support.config;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightRealProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> cfg = new HashMap<>();
        // Run Playwright in headless mode for CI/local automated runs
        cfg.put("playwright.headless", "true");
        // Keep small slow-mo for stability if needed
        cfg.put("playwright.slow-mo", "0");
        // Timeouts for navigation/network actions
        cfg.put("playwright.timeout-navigation-ms", "60000");
        cfg.put("playwright.timeout-networkidle-ms", "60000");
        cfg.put("playwright.timeout-short-ms", "1000");
        return cfg;
    }
}


