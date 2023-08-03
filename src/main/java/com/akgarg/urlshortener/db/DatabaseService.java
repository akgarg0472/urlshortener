package com.akgarg.urlshortener.db;

import com.akgarg.urlshortener.url.UrlMetadata;

import java.util.Optional;

public interface DatabaseService {

    boolean saveUrlMetadata(UrlMetadata urlMetadata);

    Optional<UrlMetadata> getUrlMetadataByShortUrl(String shortUrl);

}
