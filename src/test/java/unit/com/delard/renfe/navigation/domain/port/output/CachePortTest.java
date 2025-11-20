/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.port.output;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for CachePort interface
 * Tests the contract that all cache implementations must follow
 */
class CachePortTest
{

    /**
     * Test that CachePort is an interface (contract testing)
     * This ensures the port follows hexagonal architecture principles
     */
    @Test
    void testCachePortIsInterface()
    {
        assertTrue(CachePort.class.isInterface(), "CachePort should be an interface");
    }

    /**
     * Test that CachePort has required methods
     * This ensures the contract is properly defined
     */
    @Test
    void testCachePortMethodsExist()
    {
        try {
            CachePort.class.getMethod("get", String.class, Class.class);
            CachePort.class.getMethod("put", String.class, Object.class, long.class);
            CachePort.class.getMethod("evict", String.class);
            CachePort.class.getMethod("clear");
            CachePort.class.getMethod("isEnabled");
        } catch (NoSuchMethodException e) {
            fail("CachePort interface is missing required methods: " + e.getMessage());
        }
    }
}
