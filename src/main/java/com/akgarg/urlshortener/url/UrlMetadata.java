package com.akgarg.urlshortener.url;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UrlMetadata {

    private final String shortUrl;
    private final String originalUrl;
    private final String userId;
    private final Long createdAt;

    public UrlMetadata(final String shortUrl, final String originalUrl, final String userId, final Long createdAt) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static UrlMetadata fromShortUrl(final String shortUrl) {
        return new UrlMetadata(shortUrl, null, null, null);
    }

}
