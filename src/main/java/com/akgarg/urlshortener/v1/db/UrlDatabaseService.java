package com.akgarg.urlshortener.v1.db;

import java.util.Optional;

public interface UrlDatabaseService {

    boolean saveUrl(Url url);

    Optional<Url> getUrlByShortUrl(String shortUrl);

}
