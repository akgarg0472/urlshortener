package com.akgarg.urlshortener.url;

public record UrlMetadata(String shortUrl, String originalUrl, String userId, Long createdAt) {

    public static UrlMetadata fromShortUrl(final String shortUrl) {
        return new UrlMetadata(shortUrl, null, null, null);
    }

}
