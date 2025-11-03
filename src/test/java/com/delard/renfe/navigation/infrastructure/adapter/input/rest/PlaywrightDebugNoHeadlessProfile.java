package com.delard.renfe.navigation.infrastructure.adapter.input.rest;

import com.delard.renfe.navigation.infrastructure.adapter.output.TestTrainScraperAdapter;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightDebugNoHeadlessProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> cfg = new HashMap<>();
        // Perfil de depuración: ejecuta Playwright con ventana visible (no headless)
        cfg.put("playwright.headless", "false");
        // Aumenta el slow-mo para observar mejor las acciones en el navegador
        cfg.put("playwright.slow-mo", "200");
        // Aumenta timeouts por tratarse de navegación remota
        cfg.put("playwright.timeout-navigation-ms", "60000");
        cfg.put("playwright.timeout-networkidle-ms", "60000");
        cfg.put("playwright.timeout-short-ms", "1000");
        return cfg;
    }

    @Override
    public java.util.Set<Class<?>> getEnabledAlternatives() {
        java.util.Set<Class<?>> s = new java.util.HashSet<>();
        s.add(TestTrainScraperAdapter.class);
        return s;
    }
}


