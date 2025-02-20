package com.akgarg.urlshortener.v1.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class MongoUrlDatabaseService implements UrlDatabaseService {

    private final MongoUrlRepository mongoUrlRepository;

    @Override
    public boolean saveUrl(final Url url) {
        try {
            mongoUrlRepository.save(url);
            if (log.isDebugEnabled()) {
                log.debug("URL record saved successfully");
            }
            return true;
        } catch (Exception e) {
            log.error("Error saving url to database", e);
            return false;
        }
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        try {
            final var url = mongoUrlRepository.findByShortUrl(shortUrl);

            if (log.isDebugEnabled()) {
                log.debug("Fetched url record for '{}' is: {}", shortUrl, url.orElse(null));
            }

            return url;
        } catch (Exception e) {
            log.error("Error fetching url from database", e);
        }

        return Optional.empty();
    }

}
