package com.akgarg.urlshortener.statistics;

public record StatisticsEvent(
        Object requestId,
        EventType eventType,
        String shortUrl,
        String originalUrl,
        String userId,
        String ipAddress,
        String userAgent,
        Long createdAt,
        long eventDuration
) {
}
