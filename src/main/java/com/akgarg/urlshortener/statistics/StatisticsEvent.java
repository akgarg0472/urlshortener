package com.akgarg.urlshortener.statistics;

public record StatisticsEvent(
        String shortUrl,
        String originalUrl,
        String userId,
        String ipAddress,
        String userAgent
) {
}
