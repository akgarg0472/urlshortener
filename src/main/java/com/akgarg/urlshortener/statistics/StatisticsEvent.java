package com.akgarg.urlshortener.statistics;

public record StatisticsEvent(
        Object requestId,
        String shortUrl,
        String originalUrl,
        String userId,
        String ipAddress,
        String userAgent,
        Long urlCreatedAt,
        long eventDuration
) {
}