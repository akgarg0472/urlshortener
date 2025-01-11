package com.akgarg.urlshortener.v1.url.db;

import com.akgarg.urlshortener.v1.url.Url;

import java.util.Optional;

public interface UrlDatabaseService {

    boolean saveUrl(String requestId, Url url);

    Optional<Url> getUrlByShortUrl(String shortUrl);

}
