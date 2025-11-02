package com.renfe.navigation.infrastructure.adapter.input.rest;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightE2eProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> cfg = new HashMap<>();
        // For automated CI/local headless runs set to true
        cfg.put("playwright.headless", "true");
        // Make slowMo visible in the browser
        cfg.put("playwright.slow-mo", "200");
        // Increase navigation/network timeouts for remote site
        cfg.put("playwright.timeout-navigation-ms", "60000");
        cfg.put("playwright.timeout-networkidle-ms", "60000");
        cfg.put("playwright.timeout-short-ms", "1000");
        return cfg;
    }

    @Override
    public java.util.Set<Class<?>> getEnabledAlternatives() {
        java.util.Set<Class<?>> s = new java.util.HashSet<>();
        s.add(com.renfe.navigation.infrastructure.adapter.output.TestTrainScraperAdapter.class);
        return s;
    }
}
