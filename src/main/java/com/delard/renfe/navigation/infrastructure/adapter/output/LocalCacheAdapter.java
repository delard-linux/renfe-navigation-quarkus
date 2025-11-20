/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.output;


import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.Vetoed;

import com.delard.renfe.navigation.domain.port.output.CachePort;

import org.jboss.logging.Logger;


/**
 * Local in-memory cache adapter implementation
 * Uses ConcurrentHashMap for thread-safe operations
 * 
 * This class is @Vetoed to prevent it from being a CDI bean directly.
 * It should only be used through CacheProducer.produceCachePort()
 */
@Vetoed
public class LocalCacheAdapter implements CachePort
{

    private static final Logger LOG = Logger.getLogger(LocalCacheAdapter.class);

    private final boolean cacheEnabled;
    private final long defaultTtlSeconds;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Constructor for LocalCacheAdapter
     * 
     * @param cacheEnabled Whether cache is enabled
     * @param defaultTtlSeconds Default TTL in seconds
     */
    public LocalCacheAdapter(boolean cacheEnabled, long defaultTtlSeconds)
    {
        this.cacheEnabled = cacheEnabled;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> valueType)
    {
        if (!cacheEnabled) {
            LOG.debugf("Cache is disabled, skipping get for key: %s", key);
            return Optional.empty();
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            LOG.debugf("Cache miss for key: %s", key);
            return Optional.empty();
        }

        // Check if entry has expired
        if (entry.isExpired()) {
            LOG.debugf("Cache entry expired for key: %s", key);
            cache.remove(key);
            return Optional.empty();
        }

        try {
            @SuppressWarnings("unchecked")
            T value = (T)entry.getValue();
            LOG.debugf("Cache hit for key: %s", key);
            return Optional.of(value);
        } catch (ClassCastException e) {
            LOG.warnf(e, "Cache value type mismatch for key: %s", key);
            cache.remove(key);
            return Optional.empty();
        }
    }

    @Override
    public <T> void put(String key, T value, long ttlSeconds)
    {
        if (!cacheEnabled) {
            LOG.debugf("Cache is disabled, skipping put for key: %s", key);
            return;
        }

        long effectiveTtl = ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds;
        Instant expirationTime = effectiveTtl > 0
                ? Instant.now().plusSeconds(effectiveTtl)
                : null;

        cache.put(key, new CacheEntry(value, expirationTime));
        LOG.debugf("Cached value for key: %s with TTL: %d seconds", key, effectiveTtl);
    }

    @Override
    public void evict(String key)
    {
        if (cache.remove(key) != null) {
            LOG.debugf("Evicted cache entry for key: %s", key);
        }
    }

    @Override
    public void clear()
    {
        int size = cache.size();
        cache.clear();
        LOG.debugf("Cleared cache, removed %d entries", size);
    }

    @Override
    public boolean isEnabled()
    {
        return cacheEnabled;
    }

    /**
     * Internal cache entry with expiration support
     */
    private static class CacheEntry
    {
        private final Object value;
        private final Instant expirationTime;

        CacheEntry(Object value, Instant expirationTime)
        {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        Object getValue()
        {
            return value;
        }

        boolean isExpired()
        {
            return expirationTime != null && Instant.now().isAfter(expirationTime);
        }
    }
}
