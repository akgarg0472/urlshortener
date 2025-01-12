package com.akgarg.urlshortener.statistics;

public record StatisticsEvent(
        Object requestId,
        EventType eventType,
        String shortUrl,
        String originalUrl,
        boolean customAlias,
        String userId,
        String ipAddress,
        String userAgent,
        Long createdAt,
        long eventDuration,
        long timestamp) {
}
