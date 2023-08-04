package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.db.DatabaseService;
import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.exception.UrlShortnerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.utils.UrlLogger;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.akgarg.urlshortener.statistics.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlServiceImpl implements UrlService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(UrlServiceImpl.class);

    private final EncoderService encoderService;
    private final DatabaseService databaseService;
    private final StatisticsService statisticsService;
    private final NumberGeneratorService numberGeneratorService;
    private final String domain;

    public UrlServiceImpl(
            final EncoderService encoderService,
            final DatabaseService databaseService,
            final StatisticsService statisticsService,
            final NumberGeneratorService numberGeneratorService,
            @Value("${url.shortener.domain}") final String domain
    ) {
        this.encoderService = encoderService;
        this.databaseService = databaseService;
        this.statisticsService = statisticsService;
        this.numberGeneratorService = numberGeneratorService;
        this.domain = domain;
    }

    @Override
    public String generateShortUrl(final HttpServletRequest httpRequest, final @Valid ShortUrlRequest request) {
        LOGGER.info("Received request to short original url: {}", request);

        final var shortUrlNumber = numberGeneratorService.generateNumber();
        LOGGER.debug("NUmber generated for '{}' is {}", request.originalUrl(), shortUrlNumber);

        final var shortUrl = encoderService.encode(shortUrlNumber);
        LOGGER.debug("Encoded string for {} is {}", shortUrlNumber, shortUrl);

        final var urlMetadata = new UrlMetadata(
                shortUrl,
                request.originalUrl(),
                request.userId(),
                System.currentTimeMillis()
        );

        final var urlSaved = databaseService.saveUrlMetadata(urlMetadata);

        if (!urlSaved) {
            handleUrlSaveFailureAndThrowException(httpRequest, request, urlMetadata);
        }

        generateStatisticsEvent(httpRequest, urlMetadata, EventType.URL_CREATE_SUCCESS);

        LOGGER.info("Url shorten successfully: {}", urlMetadata);

        return domain + urlMetadata.getShortUrl();
    }

    @Override
    public URI getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        LOGGER.info("Received request to get original url from {}", shortUrl);

        final var urlMetadata = databaseService.getUrlMetadataByShortUrl(shortUrl);
        LOGGER.debug("Fetched url metadata for {} is {}", shortUrl, urlMetadata.orElse(null));

        if (urlMetadata.isEmpty()) {
            handleUrlFindFailureAndThrowException(httpRequest, shortUrl);
        }

        return URI.create(urlMetadata.get().getOriginalUrl());
    }

    private void handleUrlFindFailureAndThrowException(
            final HttpServletRequest httpRequest,
            final String shortUrl
    ) {
        LOGGER.error("Failed to retrieve original URL for {}", shortUrl);
        generateStatisticsEvent(httpRequest, UrlMetadata.of(shortUrl), EventType.URL_VISIT_FAILED);
        final var errors = new String[]{"Failed to retrieve original URL for " + shortUrl};
        throw new UrlShortnerException(errors, 404);
    }

    private void handleUrlSaveFailureAndThrowException(
            final HttpServletRequest httpRequest,
            final @Valid ShortUrlRequest request,
            final UrlMetadata urlMetadata
    ) throws UrlShortnerException {
        LOGGER.error("Shortening URL failed: {}", request);
        generateStatisticsEvent(httpRequest, urlMetadata, EventType.URL_CREATE_FAILED);
        final var errors = new String[]{"Failed to shorten URL: " + request.originalUrl()};
        throw new UrlShortnerException(errors, 500);
    }

    private void generateStatisticsEvent(
            final HttpServletRequest httpRequest,
            final UrlMetadata urlMetadata,
            final EventType eventType
    ) {
        final var statisticsEvent = new StatisticsEvent(
                urlMetadata.getShortUrl(),
                urlMetadata.getOriginalUrl(),
                urlMetadata.getUserId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("USER-AGENT"),
                urlMetadata.getCreatedAt()
        );

        LOGGER.trace("[{}] Generating statistics event: {}", eventType, statisticsEvent);

        statisticsService.publishEvent(statisticsEvent, eventType);
    }

}
