package com.delard.renfe.navigation.support.config;

import com.delard.renfe.navigation.support.stub.StubTrainScraperAdapter;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightDebugNoHeadlessProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> cfg = new HashMap<>();
        // Debug profile: runs Playwright with visible window (not headless)
        cfg.put("playwright.headless", "false");
        // Increases slow-mo to better observe actions in the browser
        cfg.put("playwright.slow-mo", "200");
        // Increases timeouts due to remote navigation
        cfg.put("playwright.timeout-navigation-ms", "60000");
        cfg.put("playwright.timeout-networkidle-ms", "60000");
        cfg.put("playwright.timeout-short-ms", "1000");
        return cfg;
    }

    @Override
    public java.util.Set<Class<?>> getEnabledAlternatives() {
        java.util.Set<Class<?>> s = new java.util.HashSet<>();
        s.add(StubTrainScraperAdapter.class);
        return s;
    }
}


