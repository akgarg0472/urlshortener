package com.akgarg.urlshortener.v1.usage.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Profile({"dev", "DEV"})
public class InMemoryUsageCacheService implements UsageCacheService {

    private static final String CUSTOM_ALIAS_KEY_PREFIX = "custom_alias:";
    private static final String CUSTOM_URL_KEY_PREFIX = "custom_url:";

    /**
     * The interval at which the eviction task runs to remove expired entries, in milliseconds.
     */
    private static final long EVICTION_INTERVAL_MS = 30 * 1000L;

    /**
     * Executor for running the eviction task at fixed intervals.
     */
    private final ScheduledExecutorService evictionExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, CounterValue> counters = new HashMap<>();

    /**
     * Starts the eviction task after the bean is initialized.
     * The eviction task will run periodically to remove expired entries from the maps.
     */
    @PostConstruct
    public void startEvictionTask() {
        evictionExecutor.scheduleAtFixedRate(this::evictExpiredEntries,
                EVICTION_INTERVAL_MS, EVICTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getCurrentCustomAliasUsage(final String requestId, final String userId, final long ttl) {
        log.debug("[{}] Getting custom alias usage for {}", requestId, userId);

        final var key = createCustomAliasKey(userId);
        final var value = counters.get(key);

        if (value == null) {
            counters.put(key, new CounterValue(0, ttl));
            return 0;
        }

        return counters.get(key).count;
    }

    @Override
    public void updateCustomAliasUsage(final String requestId, final String userId) {
        log.debug("[{}] Updating custom alias usage for {}", requestId, userId);

        final var key = createCustomAliasKey(userId);
        final var value = counters.get(key);

        if (value == null) {
            return;
        }

        value.count++;
    }

    @Override
    public int getCurrentShortUrlUsage(final String requestId, final String userId, final long ttl) {
        log.debug("[{}] Getting custom url usage for {}", requestId, userId);

        final var key = createCustomUrlKey(userId);
        final var value = counters.get(key);

        if (value == null) {
            counters.put(key, new CounterValue(0, ttl));
            return 0;
        }

        return value.count;
    }

    @Override
    public void updateShortUrlUsage(final String requestId, final String userId) {
        log.debug("[{}] Updating short url usage for {}", requestId, userId);

        final var key = createCustomUrlKey(userId);
        final var value = counters.get(key);

        if (value == null) {
            return;
        }

        value.count++;
    }

    /**
     * Periodically evicts expired entries from the maps.
     * Entries are considered expired if they are older than the TTL period.
     */
    private void evictExpiredEntries() {
        final var currentTime = System.currentTimeMillis();
        final var iterator = counters.entrySet().iterator();

        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var key = entry.getKey();
            final var expirationTime = entry.getValue().ttl;

            if (currentTime >= expirationTime) {
                counters.remove(key);
                iterator.remove();
            }
        }
    }

    /**
     * Shuts down the eviction task when the service is destroyed.
     */
    @PreDestroy
    public void stopEvictionTask() {
        evictionExecutor.shutdownNow();
    }

    private String createCustomAliasKey(final String userId) {
        return CUSTOM_ALIAS_KEY_PREFIX + userId;
    }

    private String createCustomUrlKey(final String userId) {
        return CUSTOM_URL_KEY_PREFIX + userId;
    }

    private static class CounterValue {

        private final long ttl;
        private int count;

        CounterValue(final int count, final long ttl) {
            this.count = count;
            this.ttl = ttl;
        }
    }

}
