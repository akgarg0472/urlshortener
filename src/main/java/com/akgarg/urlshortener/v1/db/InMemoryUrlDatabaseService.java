package com.akgarg.urlshortener.v1.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("dev")
@Service
@Slf4j
public class InMemoryUrlDatabaseService implements UrlDatabaseService {

    private final Map<String, Url> db;

    public InMemoryUrlDatabaseService() {
        this.db = new ConcurrentHashMap<>();
    }

    @Override
    public boolean saveUrl(final Url url) {
        log.info("Going to save URL in DB");
        db.put(url.getShortUrl(), url);
        if (log.isDebugEnabled()) {
            log.debug("URL saved in db successfully");
        }
        return true;
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        log.info("Fetching url metadata for {}", shortUrl);
        final var urlMetadata = db.get(shortUrl);
        if (log.isDebugEnabled()) {
            log.debug("Metadata fetched for {}: {}", shortUrl, urlMetadata);
        }
        return Optional.ofNullable(urlMetadata);
    }

}
