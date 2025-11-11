package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.domain.port.output.CachePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local in-memory cache adapter implementation
 * Uses ConcurrentHashMap for thread-safe operations
 */
@ApplicationScoped
public class LocalCacheAdapter implements CachePort {

    private static final Logger LOG = Logger.getLogger(LocalCacheAdapter.class);

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-enabled", defaultValue = "true")
    boolean cacheEnabled;

    @Inject
    @ConfigProperty(name = "renfe.stations-cache-ttl-seconds", defaultValue = "3600")
    long defaultTtlSeconds;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(String key, Class<T> valueType) {
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
            T value = (T) entry.getValue();
            LOG.debugf("Cache hit for key: %s", key);
            return Optional.of(value);
        } catch (ClassCastException e) {
            LOG.warnf(e, "Cache value type mismatch for key: %s", key);
            cache.remove(key);
            return Optional.empty();
        }
    }

    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
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
    public void evict(String key) {
        if (cache.remove(key) != null) {
            LOG.debugf("Evicted cache entry for key: %s", key);
        }
    }

    @Override
    public void clear() {
        int size = cache.size();
        cache.clear();
        LOG.debugf("Cleared cache, removed %d entries", size);
    }

    @Override
    public boolean isEnabled() {
        return cacheEnabled;
    }

    /**
     * Internal cache entry with expiration support
     */
    private static class CacheEntry {
        private final Object value;
        private final Instant expirationTime;

        CacheEntry(Object value, Instant expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        Object getValue() {
            return value;
        }

        boolean isExpired() {
            return expirationTime != null && Instant.now().isAfter(expirationTime);
        }
    }
}

