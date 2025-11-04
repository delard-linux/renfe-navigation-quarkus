package com.delard.renfe.navigation.infrastructure.service;

import com.microsoft.playwright.Playwright;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple factory to centralize the creation of Playwright instances.
 * Having this abstraction allows tests to mock the creation step and avoid
 * launching a real browser.
 */
@ApplicationScoped
public class PlaywrightFactory {

    public Playwright create() {
        return Playwright.create();
    }
}


