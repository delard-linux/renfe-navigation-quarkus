package com.renfe.navigation.infrastructure.adapter.input.rest;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightE2eProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> cfg = new HashMap<>();
        // Run Playwright in non-headless mode for E2E observation
        cfg.put("playwright.headless", "false");
        // Make slowMo visible in the browser
        cfg.put("playwright.slow-mo", "200");
        // Increase navigation/network timeouts for remote site
        cfg.put("playwright.timeout-navigation-ms", "60000");
        cfg.put("playwright.timeout-networkidle-ms", "60000");
        cfg.put("playwright.timeout-short-ms", "1000");
        return cfg;
    }
}

