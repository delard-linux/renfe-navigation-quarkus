/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microsoft.playwright.Playwright;


/**
 * Unit tests for PlaywrightFactory
 */
@ExtendWith(MockitoExtension.class)
class PlaywrightFactoryTest
{

    @Test
    void testCreate()
    {
        PlaywrightFactory factory = new PlaywrightFactory();

        try (MockedStatic<Playwright> playwrightMock = mockStatic(Playwright.class)) {
            Playwright mockPlaywright = mock(Playwright.class);
            playwrightMock.when(Playwright::create).thenReturn(mockPlaywright);

            Playwright result = factory.create();

            assertNotNull(result);
            assertEquals(mockPlaywright, result);
            playwrightMock.verify(Playwright::create, times(1));
        }
    }
}
