package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.UrlMetadata;

import java.util.Optional;

public interface DatabaseService {

    boolean saveUrlMetadata(UrlMetadata urlMetadata);

    Optional<UrlMetadata> getUrlMetadataByShortUrl(String shortUrl);

}
