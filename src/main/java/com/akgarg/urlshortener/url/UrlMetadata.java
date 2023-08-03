package com.akgarg.urlshortener.url;

public record UrlMetadata(
        String shortUrl,
        String originalUrl,
        String userId
) {
}
