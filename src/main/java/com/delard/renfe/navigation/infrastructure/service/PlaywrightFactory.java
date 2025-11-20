/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.service;


import jakarta.enterprise.context.ApplicationScoped;

import com.microsoft.playwright.Playwright;


/**
 * Simple factory to centralize the creation of Playwright instances.
 * Having this abstraction allows tests to mock the creation step and avoid
 * launching a real browser.
 */
@ApplicationScoped
public class PlaywrightFactory
{

    public Playwright create()
    {
        return Playwright.create();
    }
}
