package com.delard.renfe.navigation.support.config;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Test profile for Playwright integration tests.
 * This profile loads configuration from application-integration.properties
 * which contains Playwright settings for debugging (headless=false, slow-mo, etc.).
 * 
 * Usage: Add @TestProfile(PlaywrightIntegrationTestProfile.class) to your test class.
 */
public class PlaywrightIntegrationTestProfile implements QuarkusTestProfile {
    
    @Override
    public String getConfigProfile() {
        return "integration";
    }
}

