package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.db.DatabaseService;
import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.exception.UrlShortnerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.akgarg.urlshortener.statistics.StatisticsService;
import com.akgarg.urlshortener.utils.UrlLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

import static com.akgarg.urlshortener.utils.UrlShortenerUtility.extractRequestIdFromRequest;

@Service
public class DefaultUrlService implements UrlService {

    private static final UrlLogger LOGGER = UrlLogger.getLogger(DefaultUrlService.class);

    private final EncoderService encoderService;
    private final DatabaseService databaseService;
    private final StatisticsService statisticsService;
    private final NumberGeneratorService numberGeneratorService;
    private final String domain;

    public DefaultUrlService(
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
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var startTime = System.currentTimeMillis();

        LOGGER.info("[{}]: Received: {}", requestId, request);

        final var shortUrlNumber = numberGeneratorService.generateNumber();
        LOGGER.debug("[{}]: Number generated for '{}' is {}", requestId, request.originalUrl(), shortUrlNumber);

        if (shortUrlNumber <= 0) {
            LOGGER.error("[{}]: Failed to generate number for {}", requestId, request);
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        final var shortUrl = encoderService.encode(shortUrlNumber);
        LOGGER.debug("[{}]: Encoded string for {} is {}", requestId, shortUrlNumber, shortUrl);

        final var urlMetadata = new UrlMetadata(shortUrl, request.originalUrl(), request.userId(), System.currentTimeMillis());

        final var urlSaved = databaseService.saveUrlMetadata(urlMetadata);

        if (!urlSaved) {
            LOGGER.error("[{}]: Shortening URL failed: {}", requestId, request);
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        generateStatisticsEvent(httpRequest, urlMetadata, EventType.URL_CREATE_SUCCESS, startTime);

        LOGGER.info("[{}]: Url shorten successfully: {}", requestId, urlMetadata);

        return domain + urlMetadata.getShortUrl();
    }

    @Override
    public URI getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        final var requestId = extractRequestIdFromRequest(httpRequest);
        LOGGER.info("[{}]: Received request to get original url for {}", requestId, shortUrl);

        final var startTime = System.currentTimeMillis();
        final var urlMetadata = databaseService.getUrlMetadataByShortUrl(shortUrl);

        LOGGER.debug("[{}]: Metadata fetched for {} is {}", requestId, shortUrl, urlMetadata.orElse(null));

        if (urlMetadata.isEmpty()) {
            LOGGER.error("[{}]: Failed to retrieve original URL for {}", requestId, shortUrl);
            handleUrlFindFailureAndThrowException(httpRequest, shortUrl, startTime);
            return null;
        }

        final var originalUri = URI.create(urlMetadata.get().getOriginalUrl());

        generateStatisticsEvent(httpRequest, urlMetadata.get(), EventType.URL_GET_SUCCESS, startTime);

        LOGGER.debug("[{}]: Original URI for {} is {}", requestId, shortUrl, originalUri);

        return originalUri;
    }

    private void handleShorteningFailureAndThrowException(
            final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime
    ) {
        generateStatisticsEvent(
                httpRequest,
                UrlMetadata.fromShortUrl(request.originalUrl()),
                EventType.URL_CREATE_FAILED,
                startTime
        );

        throw new UrlShortnerException(
                new String[]{"Error shortening url: " + request.originalUrl()},
                500
        );
    }

    private void handleUrlFindFailureAndThrowException(
            final HttpServletRequest httpRequest, final String shortUrl, final long startTime
    ) throws UrlShortnerException {
        generateStatisticsEvent(
                httpRequest,
                UrlMetadata.fromShortUrl(shortUrl),
                EventType.URL_GET_FAILED,
                startTime
        );

        throw new UrlShortnerException(
                new String[]{shortUrl + " not found"},
                404
        );
    }

    private void generateStatisticsEvent(
            final HttpServletRequest httpRequest,
            final UrlMetadata urlMetadata,
            final EventType eventType,
            final long startTime
    ) {
        final var eventDuration = System.currentTimeMillis() - startTime;
        final var requestId = extractRequestIdFromRequest(httpRequest);

        final var statisticsEvent = new StatisticsEvent(
                requestId,
                eventType,
                urlMetadata.getShortUrl(),
                urlMetadata.getOriginalUrl(),
                urlMetadata.getUserId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("USER-AGENT"),
                urlMetadata.getCreatedAt(),
                eventDuration
        );

        statisticsService.publishEvent(statisticsEvent);
    }

}
