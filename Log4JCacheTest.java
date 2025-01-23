package com.lseg.ipps.solutions.tpl.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class Log4JCacheTest {

    private Log4JCache cache;
    private static final String TEST_KEY = "test-key";
    private static final String TEST_VALUE = "test-value";

    @BeforeEach
    void setUp() {
        cache = Log4JCache.getInstance(TimeUnit.SECONDS, 1);
    }

    @AfterEach
    void tearDown() {
        cache.shutdown();
    }

    @Test
    void getInstance_ReturnsSameInstance() {
        Log4JCache instance1 = Log4JCache.getInstance(TimeUnit.SECONDS, 1);
        Log4JCache instance2 = Log4JCache.getInstance(TimeUnit.SECONDS, 1);
        
        assertSame(instance1, instance2);
    }

    @Test
    void put_StoresValueInCache() {
        cache.put(TEST_KEY, TEST_VALUE);
        
        assertEquals(TEST_VALUE, cache.get(TEST_KEY));
    }

    @Test
    void putIfAbsent_WhenKeyDoesNotExist_StoresValue() {
        Object result = cache.putIfAbsent(TEST_KEY, TEST_VALUE);
        
        assertNull(result);
        assertEquals(TEST_VALUE, cache.get(TEST_KEY));
    }

    @Test
    void putIfAbsent_WhenKeyExists_ReturnsPreviousValue() {
        cache.put(TEST_KEY, TEST_VALUE);
        Object result = cache.putIfAbsent(TEST_KEY, "new-value");
        
        assertEquals(TEST_VALUE, result);
        assertEquals(TEST_VALUE, cache.get(TEST_KEY));
    }

    @Test
    void remove_WhenValueMatches_RemovesEntry() {
        cache.put(TEST_KEY, TEST_VALUE);
        boolean removed = cache.remove(TEST_KEY, TEST_VALUE);
        
        assertTrue(removed);
        assertNull(cache.get(TEST_KEY));
    }

    @Test
    void remove_WhenValueDoesNotMatch_RetainEntry() {
        cache.put(TEST_KEY, TEST_VALUE);
        boolean removed = cache.remove(TEST_KEY, "wrong-value");
        
        assertFalse(removed);
        assertEquals(TEST_VALUE, cache.get(TEST_KEY));
    }

    @Test
    void clearAll_RemovesEntry() {
        cache.put(TEST_KEY, TEST_VALUE);
        cache.clearAll(TEST_KEY);
        
        assertNull(cache.get(TEST_KEY));
    }

    @Test
    void checkIfLog4JCacheIsNull_AfterInitialization_ReturnsFalse() {
        assertFalse(Log4JCache.checkIfLog4JCacheIsNull());
    }

    @Test
    void computeIfAbsent_ComputesNewValue() {
        String result = (String) cache.computeIfAbsent(TEST_KEY, k -> TEST_VALUE);
        
        assertEquals(TEST_VALUE, result);
        assertEquals(TEST_VALUE, cache.get(TEST_KEY));
    }
}