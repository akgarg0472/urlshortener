package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("dev")
@Service
public class InMemoryUrlDatabaseService implements UrlDatabaseService {

    private static final Logger LOGGER = LogManager.getLogger(InMemoryUrlDatabaseService.class);

    private final Map<String, Url> db;

    public InMemoryUrlDatabaseService() {
        this.db = new ConcurrentHashMap<>();
    }

    @Override
    public boolean saveUrl(final Url url) {
        LOGGER.info("Going to save {} in DB", url);
        db.put(url.getShortUrl(), url);
        LOGGER.debug("{} saved in db successfully", url);
        return true;
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        LOGGER.info("Fetching url metadata for {}", shortUrl);
        final var urlMetadata = db.get(shortUrl);
        LOGGER.trace("Metadata fetched for {}: {}", shortUrl, urlMetadata);
        return Optional.ofNullable(urlMetadata);
    }

}
