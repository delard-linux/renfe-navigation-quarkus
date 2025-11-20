/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.port.output;


import java.util.Optional;


/**
 * Output port for caching operations
 * Supports different cache implementations (local, Redis, etc.)
 */
public interface CachePort
{

    /**
     * Get a value from cache
     *
     * @param key Cache key
     * @return Optional containing the cached value, or empty if not found or expired
     */
    <T> Optional<T> get(String key, Class<T> valueType);

    /**
     * Put a value in cache
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttlSeconds Time to live in seconds (0 or negative means no expiration)
     */
    <T> void put(String key, T value, long ttlSeconds);

    /**
     * Remove a value from cache
     *
     * @param key Cache key
     */
    void evict(String key);

    /**
     * Clear all cache entries
     */
    void clear();

    /**
     * Check if cache is enabled
     *
     * @return true if cache is enabled, false otherwise
     */
    boolean isEnabled();
}
