package com.akgarg.urlshortener.v1.usage.cache;

public interface UsageCacheService {

    int getCurrentCustomAliasUsage(String requestId, String userId, final long ttl);

    void updateCustomAliasUsage(String requestId, String userId);

    int getCurrentShortUrlUsage(String requestId, String userId, final long ttl);

    void updateShortUrlUsage(String requestId, String userId);

}
