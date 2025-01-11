package com.akgarg.urlshortener.v1.url.db;

import com.akgarg.urlshortener.v1.url.Url;
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
    public boolean saveUrl(final String requestId, final Url url) {
        try {
            mongoUrlRepository.save(url);
            log.info("[{}] URL record saved successfully", requestId);
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

            if (url.isEmpty()) {
                log.error("No url record found for shortUrl: {}", shortUrl);
                return Optional.empty();
            }

            log.debug("Fetched url record for '{}' is: {}", shortUrl, url.get());
            return url;
        } catch (Exception e) {
            log.error("Error fetching url from database", e);
        }

        return Optional.empty();
    }

}
