/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.config;


import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import com.delard.renfe.navigation.domain.port.output.CachePort;
import com.delard.renfe.navigation.infrastructure.adapter.output.LocalCacheAdapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for CacheProducer
 */
class CacheProducerTest
{

    private CacheProducer cacheProducer;

    @BeforeEach
    void setUp()
    {
        cacheProducer = new CacheProducer();
    }

    @Test
    void testProduceCachePortWithLocalType() throws Exception
    {
        // Set cache type to "local"
        setField("cacheType", "local");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
        assertTrue(result.isEnabled());
    }

    @Test
    void testProduceCachePortWithRedisType() throws Exception
    {
        // Set cache type to "redis" (should fallback to local)
        setField("cacheType", "redis");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        // Redis is not implemented yet, so it should fallback to LocalCacheAdapter
        assertInstanceOf(LocalCacheAdapter.class, result);
        assertTrue(result.isEnabled());
    }

    @Test
    void testProduceCachePortWithUnknownType() throws Exception
    {
        // Set cache type to unknown value (should fallback to local)
        setField("cacheType", "unknown");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
        assertTrue(result.isEnabled());
    }

    @Test
    void testProduceCachePortWithCaseInsensitiveType() throws Exception
    {
        // Test that cache type is case-insensitive
        setField("cacheType", "LOCAL");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
    }

    @Test
    void testProduceCachePortWithMixedCaseType() throws Exception
    {
        // Test that cache type is case-insensitive
        setField("cacheType", "LoCaL");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
    }

    @Test
    void testProduceCachePortWithCacheDisabled() throws Exception
    {
        // Test with cache disabled
        setField("cacheType", "local");
        setField("cacheEnabled", false);
        setField("defaultTtlSeconds", 3600L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
        assertFalse(result.isEnabled());
    }

    @Test
    void testProduceCachePortWithCustomTtl() throws Exception
    {
        // Test with custom TTL
        setField("cacheType", "local");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 7200L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
        assertTrue(result.isEnabled());
    }

    @Test
    void testProduceCachePortWithZeroTtl() throws Exception
    {
        // Test with zero TTL
        setField("cacheType", "local");
        setField("cacheEnabled", true);
        setField("defaultTtlSeconds", 0L);

        CachePort result = cacheProducer.produceCachePort();

        assertNotNull(result);
        assertInstanceOf(LocalCacheAdapter.class, result);
    }

    /**
     * Helper method to set a private field using reflection
     *
     * @param fieldName Name of the field to set
     * @param value Value to set
     * @throws Exception if reflection fails
     */
    private void setField(String fieldName, Object value) throws Exception
    {
        Field field = CacheProducer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(cacheProducer, value);
    }
}
