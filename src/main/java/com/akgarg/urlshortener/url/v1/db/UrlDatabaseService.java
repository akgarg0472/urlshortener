package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;

import java.util.Optional;

public interface UrlDatabaseService {

    boolean saveUrl(Url url);

    Optional<Url> getUrlByShortUrl(String shortUrl);

}
