package com.akgarg.urlshortener.v1.usage;

import com.akgarg.urlshortener.v1.usage.cache.UsageCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageTrackingService {

    private final UsageCacheService usageCacheService;

    public int getCurrentCustomAliasUsageForUser(final String requestId, final String userId, final long expiresAt) {
        log.info("[{}] Getting current custom alias usage for user {}", requestId, userId);
        return usageCacheService.getCurrentCustomAliasUsage(requestId, userId, expiresAt - System.currentTimeMillis());
    }

    public void increaseCustomAliasUsageByOne(final String requestId, final String userId) {
        log.info("[{}] Updating custom alias usage for user {}", requestId, userId);
        usageCacheService.updateCustomAliasUsage(requestId, userId);
    }

    public int getCurrentShortUrlUsageForUser(final String requestId, final String userId, final long expiresAt) {
        log.info("[{}] Getting current short url usage for user {}", requestId, userId);
        return usageCacheService.getCurrentShortUrlUsage(requestId, userId, expiresAt - System.currentTimeMillis());
    }

    public void increaseShortUrlUsageByOne(final String requestId, final String userId) {
        log.info("[{}] Updating short url usage for user {}", requestId, userId);
        usageCacheService.updateShortUrlUsage(requestId, userId);
    }

}
