package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile("prod")
@Service
@RequiredArgsConstructor
public class MySQLUrlDatabaseService implements UrlDatabaseService {

    private static final Logger LOGGER = LogManager.getLogger(MySQLUrlDatabaseService.class);

    private final UrlRepository urlRepository;

    @Override
    public boolean saveUrl(final Url url) {
        try {
            final Url savedUrl = urlRepository.save(url);
            LOGGER.info("URL record saved successfully: {}", savedUrl);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error saving url to database", e);
            return false;
        }
    }

    @Override
    public Optional<Url> getUrlByShortUrl(final String shortUrl) {
        try {
            final Optional<Url> url = urlRepository.findById(shortUrl);

            if (url.isEmpty()) {
                LOGGER.error("No url record found for shortUrl: {}", shortUrl);
                return Optional.empty();
            }

            LOGGER.debug("Fetched url record for '{}' is: {}", shortUrl, url.get());
            return url;
        } catch (Exception e) {
            LOGGER.error("Error fetching url from database", e);
        }

        return Optional.empty();
    }

}