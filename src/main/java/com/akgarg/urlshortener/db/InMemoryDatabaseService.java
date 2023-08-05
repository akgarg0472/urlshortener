package com.akgarg.urlshortener.db;

import com.akgarg.urlshortener.url.UrlMetadata;
import com.akgarg.urlshortener.utils.UrlLogger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryDatabaseService implements DatabaseService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(InMemoryDatabaseService.class);

    private final Map<String, UrlMetadata> db;

    public InMemoryDatabaseService() {
        this.db = new ConcurrentHashMap<>();
    }

    @Override
    public boolean saveUrlMetadata(final UrlMetadata urlMetadata) {
        LOGGER.info("Going to save {} in DB", urlMetadata);
        db.put(urlMetadata.getShortUrl(), urlMetadata);
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
