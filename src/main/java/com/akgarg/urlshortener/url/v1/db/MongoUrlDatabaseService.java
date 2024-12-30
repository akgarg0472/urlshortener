package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
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

    private final UrlRepository urlRepository;

    @Override
    public boolean saveUrl(final Url url) {
        try {
            final var savedUrl = urlRepository.save(url);
            log.info("URL record saved successfully: {}", savedUrl);
            return true;
        } catch (Exception e) {
            log.error("Error saving url to database", e);
            return false;
        }
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        try {
            final var url = urlRepository.findByShortUrl(shortUrl);

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
