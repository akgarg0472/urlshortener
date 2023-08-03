package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.db.DatabaseService;
import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.exception.UrlShortnerException;
import com.akgarg.urlshortener.logger.UrlLogger;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.akgarg.urlshortener.statistics.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlServiceImpl implements UrlService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(UrlServiceImpl.class);

    private final EncoderService encoderService;
    private final DatabaseService databaseService;
    private final StatisticsService statisticsService;

    public UrlServiceImpl(
            final DatabaseService databaseService,
            final EncoderService encoderService,
            final StatisticsService statisticsService
    ) {
        this.databaseService = databaseService;
        this.encoderService = encoderService;
        this.statisticsService = statisticsService;
    }

    @Override
    public String generateShortUrl(final HttpServletRequest httpRequest, final @Valid ShortUrlRequest request) {
        LOGGER.info("Received request to short original url: {}", request);

        final String shortUrl = encoderService.encode(request.originalUrl());
        LOGGER.debug("Encoder service response for {} is {}", request, shortUrl);

        final var urlMetadata = new UrlMetadata(shortUrl, request.originalUrl(), request.userId());
        final boolean urlSaved = databaseService.saveUrlMetadata(urlMetadata);

        if (!urlSaved) {
            handleUrlSaveFailure(httpRequest, request, urlMetadata);
        }

        publishStatisticsEvent(httpRequest, urlMetadata, EventType.URL_CREATE_SUCCESS);

        LOGGER.info("Url shorten successfully: {}", urlMetadata);

        return urlMetadata.shortUrl();
    }

    @Override
    public URI getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        LOGGER.info("Received request to get original url from {}", shortUrl);

        final var urlMetadata = databaseService.getUrlMetadataByShortUrl(shortUrl);
        LOGGER.debug("Fetched url metadata for {} is {}", shortUrl, urlMetadata.orElse(null));

        if (urlMetadata.isEmpty()) {
            handleUrlRetrieveNotFound(httpRequest, shortUrl);
            return null;
        }

        return URI.create(urlMetadata.get().originalUrl());
    }

    private void handleUrlRetrieveNotFound(
            final HttpServletRequest httpRequest,
            final String shortUrl
    ) throws UrlShortnerException {
        LOGGER.error("Failed to retrieve original URL for {}", shortUrl);

        publishStatisticsEvent(httpRequest, new UrlMetadata(shortUrl, null, null), EventType.URL_VISIT_FAILED);

        final var errors = new String[]{"Failed to retrieve original URL for " + shortUrl};
        throw new UrlShortnerException(errors, 404);
    }

    private void handleUrlSaveFailure(
            final HttpServletRequest httpRequest,
            final @Valid ShortUrlRequest request,
            final UrlMetadata urlMetadata
    ) throws UrlShortnerException {
        LOGGER.error("Error shortening URL: ", request);

        publishStatisticsEvent(httpRequest, urlMetadata, EventType.URL_CREATE_FAILED);

        final var errors = new String[]{"Failed to shorten URL: " + request.originalUrl()};
        throw new UrlShortnerException(errors, 500);
    }

    private void publishStatisticsEvent(
            final HttpServletRequest httpRequest,
            final UrlMetadata urlMetadata,
            final EventType eventType
    ) {
        final var statisticsEvent = new StatisticsEvent(
                urlMetadata.shortUrl(),
                urlMetadata.originalUrl(),
                urlMetadata.userId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("USER-AGENT")
        );

        statisticsService.publishEvent(statisticsEvent, eventType);
    }

}
