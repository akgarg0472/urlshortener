package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        log.info("Going to save {} in DB", url);
        db.put(url.getShortUrl(), url);
        log.debug("{} saved in db successfully", url);
        return true;
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        log.info("Fetching url metadata for {}", shortUrl);
        final var urlMetadata = db.get(shortUrl);
        log.trace("Metadata fetched for {}: {}", shortUrl, urlMetadata);
        return Optional.ofNullable(urlMetadata);
    }

}
