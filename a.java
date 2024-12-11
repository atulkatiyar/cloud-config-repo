package com.lseg.ipps.solutions.tpl.cache;

import java.util.concurrent.*;
import java.util.function.Function;

public class Log4JCache {

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> timestamps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final TimeUnit timeUnit;

    private final long expirationDuration;

    private static Log4JCache instance;

    public static Log4JCache getInstance(TimeUnit timeUnit, long expirationDuration) {
        if (instance == null) {
            synchronized (Log4JCache.class) {
                if (instance == null) {
                    instance = new Log4JCache(timeUnit, expirationDuration);
                }
            }
        }
        return instance;
    }

    public static boolean checkIfLog4JCacheIsNull() {
        return instance == null;
    }

    private Log4JCache(TimeUnit timeUnit, long expirationDuration) {
        this.timeUnit = timeUnit;
        this.expirationDuration = expirationDuration;
        try {
            scheduler.scheduleAtFixedRate(
                    this::removeExpiredEntries, 0, expirationDuration, timeUnit);
        } catch (Exception e) {
        }
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public Object put(String key, Object value) {
        timestamps.put(key, System.nanoTime());
        return cache.put(key, value);
    }

    public Object putIfAbsent(String key, Object value) {
        timestamps.putIfAbsent(key, System.nanoTime());
        return cache.putIfAbsent(key, value);
    }

    public Object computeIfAbsent(
            String key, Function<? super String, ? extends Object> mappingFunction) {
        timestamps.putIfAbsent(key, System.nanoTime());
        return cache.computeIfAbsent(
                key,
                k -> {
                    Object value = mappingFunction.apply(k);
                    timestamps.put(k, System.nanoTime());
                    return value;
                });
    }

    public boolean remove(String key, Object value) {
        timestamps.remove(key);
        return cache.remove(key, value);
    }

    public void clearAll(String key) {
        timestamps.remove(key);
        cache.remove(key);
    }

    private void removeExpiredEntries() {
        long expirationThreshold =  System.nanoTime() - timeUnit.toNanos(expirationDuration) + 5000000; // Adjusted the latency
        System.out.println( "timer map " + timestamps.get("admin_timer")/1000/1000);
        System.out.println( " system time " + expirationThreshold/1000/1000);
        for (String key : timestamps.keySet()) {
            if (timestamps.get(key) < expirationThreshold) {
                timestamps.remove(key);
                cache.remove(key);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
