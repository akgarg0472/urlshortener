package com.akgarg.urlshortener.db;

import com.akgarg.urlshortener.url.UrlMetadata;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DatabaseServiceImpl implements DatabaseService {

    @Override
    public boolean saveUrlMetadata(final UrlMetadata urlMetadata) {
        return false;
    }

    @Override
    public Optional<UrlMetadata> getUrlMetadataByShortUrl(final String shortUrl) {
        return Optional.empty();
    }

}
