/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.config;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.delard.renfe.navigation.domain.port.output.CachePort;
import com.delard.renfe.navigation.infrastructure.adapter.output.LocalCacheAdapter;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;


/**
 * Producer for CachePort implementations
 * Selects the appropriate cache adapter based on renfe.stations-cache-type property
 * 
 * Supported cache types:
 * - "local": Uses LocalCacheAdapter (in-memory cache with ConcurrentHashMap)
 * - "redis": Future implementation using RedisCacheAdapter (not yet implemented)
 * 
 * Default: "local"
 */
@ApplicationScoped
public class CacheProducer
{

    private static final Logger LOG = Logger.getLogger(CacheProducer.class);

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-type", defaultValue = "local")
    String cacheType;

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-enabled", defaultValue = "true")
    boolean cacheEnabled;

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-ttl-seconds", defaultValue = "3600")
    long defaultTtlSeconds;

    // Future: RedisCacheAdapter redisCacheAdapter;

    /**
     * Produces the appropriate CachePort implementation based on configuration
     * 
     * @return CachePort implementation (LocalCacheAdapter or future RedisCacheAdapter)
     */
    @Produces
    @ApplicationScoped
    public CachePort produceCachePort()
    {
        LOG.infof("Creating cache adapter of type: %s", cacheType);

        switch (cacheType.toLowerCase()) {
            case "local":
                LOG.debugf("Using LocalCacheAdapter for cache");
                return new LocalCacheAdapter(cacheEnabled, defaultTtlSeconds);

            case "redis":
                // return redisCacheAdapter;
                LOG.warnf("Redis cache adapter not yet implemented, falling back to local cache");
                return new LocalCacheAdapter(cacheEnabled, defaultTtlSeconds);

            default:
                LOG.warnf("Unknown cache type '%s', using local cache as fallback", cacheType);
                return new LocalCacheAdapter(cacheEnabled, defaultTtlSeconds);
        }
    }
}
