package com.delard.renfe.navigation.infrastructure.adapter.output;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocalCacheAdapter
 * 
 * Note: These tests verify basic cache functionality.
 * Cache expiration and TTL are tested in integration tests due to time dependencies.
 */
class LocalCacheAdapterTest {

    private LocalCacheAdapter localCacheAdapter;

    @BeforeEach
    void setUp() {
        localCacheAdapter = new LocalCacheAdapter();
        
        // Enable cache for testing using reflection
        try {
            java.lang.reflect.Field field = LocalCacheAdapter.class.getDeclaredField("cacheEnabled");
            field.setAccessible(true);
            field.set(localCacheAdapter, true);
        } catch (Exception e) {
            fail("Failed to enable cache for testing: " + e.getMessage());
        }
    }

    @Test
    void testPutAndGet() {
        String key = "test:key";
        String value = "test value";

        localCacheAdapter.put(key, value, 60);

        Optional<String> result = localCacheAdapter.get(key, String.class);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    void testGetNonExistentKey() {
        Optional<String> result = localCacheAdapter.get("non:existent", String.class);

        assertFalse(result.isPresent());
    }

    @Test
    void testEvict() {
        String key = "test:key";
        String value = "test value";

        localCacheAdapter.put(key, value, 60);
        localCacheAdapter.evict(key);

        Optional<String> result = localCacheAdapter.get(key, String.class);
        assertFalse(result.isPresent());
    }

    @Test
    void testClear() {
        localCacheAdapter.put("key1", "value1", 60);
        localCacheAdapter.put("key2", "value2", 60);

        localCacheAdapter.clear();

        assertFalse(localCacheAdapter.get("key1", String.class).isPresent());
        assertFalse(localCacheAdapter.get("key2", String.class).isPresent());
    }

    @Test
    void testPutListAndGet() {
        String key = "test:list";
        List<String> value = List.of("item1", "item2", "item3");

        localCacheAdapter.put(key, value, 60);

        @SuppressWarnings("unchecked")
        Optional<List<String>> result = (Optional<List<String>>) (Optional<?>) localCacheAdapter.get(key, List.class);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
        assertEquals("item1", result.get().get(0));
    }

    @Test
    void testCacheIsEnabled() {
        assertTrue(localCacheAdapter.isEnabled());
    }

    @Test
    void testCacheDisabled() {
        try {
            java.lang.reflect.Field field = LocalCacheAdapter.class.getDeclaredField("cacheEnabled");
            field.setAccessible(true);
            field.set(localCacheAdapter, false);
        } catch (Exception e) {
            fail("Failed to disable cache for testing: " + e.getMessage());
        }

        localCacheAdapter.put("test:key", "value", 60);
        Optional<String> result = localCacheAdapter.get("test:key", String.class);

        assertFalse(result.isPresent(), "Cache should not return values when disabled");
    }
}
