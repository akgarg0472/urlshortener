package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.UrlMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDatabaseService implements DatabaseService {

    private static final Logger LOGGER = LogManager.getLogger(InMemoryDatabaseService.class);

    private final Map<String, UrlMetadata> db;

    public InMemoryDatabaseService() {
        this.db = new ConcurrentHashMap<>();
    }

    @Override
    public boolean saveUrlMetadata(final UrlMetadata urlMetadata) {
        LOGGER.info("Going to save {} in DB", urlMetadata);
        db.put(urlMetadata.shortUrl(), urlMetadata);
        LOGGER.debug("{} saved in db successfully", urlMetadata);
        return true;
    }

    @Override
    public Optional<UrlMetadata> getUrlMetadataByShortUrl(final String shortUrl) {
        LOGGER.info("Fetching url metadata for {}", shortUrl);
        final var urlMetadata = db.get(shortUrl);
        LOGGER.trace("Metadata fetched for {}: {}", shortUrl, urlMetadata);
        return Optional.ofNullable(urlMetadata);
    }

}
