package com.akgarg.urlshortener.v1.usage.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod", "PROD"})
public class RedisUsageCacheService implements UsageCacheService {

    private static final String CUSTOM_ALIAS_KEY_PREFIX = "custom_alias:";
    private static final String CUSTOM_URL_KEY_PREFIX = "custom_url:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public int getCurrentCustomAliasUsage(final String requestId, final String userId, final long ttl) {
        log.debug("[{}] Getting custom alias usage for {}", requestId, userId);

        try {
            final var key = createCustomAliasKey(userId);
            final var counter = redisTemplate.opsForValue().get(key);

            if (Objects.isNull(counter)) {
                redisTemplate.opsForValue().set(key, "0", ttl, TimeUnit.MILLISECONDS);
                return 0;
            }

            return Integer.parseInt(counter);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void updateCustomAliasUsage(final String requestId, final String userId) {
        log.debug("[{}] Updating custom alias usage for {}", requestId, userId);

        try {
            redisTemplate.opsForValue().increment(createCustomAliasKey(userId), 1);
        } catch (Exception e) {
            log.error("[{}] Failed to update custom alias usage for {}", requestId, userId);
        }
    }

    @Override
    public int getCurrentShortUrlUsage(final String requestId, final String userId, final long ttl) {
        log.debug("[{}] Getting current short url usage for {}", requestId, userId);

        try {
            final var key = createCustomUrlKey(userId);
            final var counter = redisTemplate.opsForValue().get(key);

            if (Objects.isNull(counter)) {
                redisTemplate.opsForValue().set(key, "0", ttl, TimeUnit.MILLISECONDS);
                return 0;
            }

            return Integer.parseInt(counter);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void updateShortUrlUsage(final String requestId, final String userId) {
        log.debug("[{}] Updating custom url usage for {}", requestId, userId);

        try {
            redisTemplate.opsForValue().increment(createCustomUrlKey(userId), 1);
        } catch (Exception e) {
            log.error("[{}] Failed to update short url usage for {}", requestId, userId);
        }
    }

    private String createCustomAliasKey(final String userId) {
        return CUSTOM_ALIAS_KEY_PREFIX + userId;
    }

    private String createCustomUrlKey(final String userId) {
        return CUSTOM_URL_KEY_PREFIX + userId;
    }
}
