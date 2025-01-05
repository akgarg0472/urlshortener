package com.akgarg.urlshortener.url.v1;

import com.akgarg.urlshortener.customalias.v1.CustomAliasService;
import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.exception.UrlShortenerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
import com.akgarg.urlshortener.statistics.EventType;
import com.akgarg.urlshortener.statistics.StatisticsEvent;
import com.akgarg.urlshortener.statistics.StatisticsService;
import com.akgarg.urlshortener.url.v1.db.UrlDatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.extractRequestIdFromRequest;

@Service
@Slf4j
public class UrlService {

    private final EncoderService encoderService;
    private final UrlDatabaseService urlDatabaseService;
    private final StatisticsService statisticsService;
    private final NumberGeneratorService numberGeneratorService;
    private final CustomAliasService customAliasService;
    private final String domain;

    public UrlService(
            final EncoderService encoderService,
            final UrlDatabaseService urlDatabaseService,
            final StatisticsService statisticsService,
            final NumberGeneratorService numberGeneratorService,
            final CustomAliasService customAliasService,
            @Value("${url.shortener.domain}") final String domain
    ) {
        this.encoderService = encoderService;
        this.urlDatabaseService = urlDatabaseService;
        this.statisticsService = statisticsService;
        this.numberGeneratorService = numberGeneratorService;
        this.customAliasService = customAliasService;
        this.domain = domain.endsWith("/") ? domain : domain + "/";
    }

    public GenerateUrlResponse generateShortUrl(
            final HttpServletRequest httpRequest,
            @Valid final ShortUrlRequest request
    ) {
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var startTime = System.currentTimeMillis();

        log.info("[{}]: Received: {}", requestId, request);

        final long expirationTime;

        if (request.expiresAt() != null) {
            expirationTime = request.expiresAt();
        } else {
            expirationTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 100L);
        }

        if (expirationTime < System.currentTimeMillis()) {
            log.info("{} invalid expiration time", requestId);
            throw new UrlShortenerException(
                    new String[]{"Invalid expiration time"},
                    400,
                    "URL Expiration time is past date: " + expirationTime
            );
        }

        final var customAlias = request.customAlias() != null && !request.customAlias().isBlank();

        if (customAlias) {
            final var validate = customAliasService.validate(requestId, request);

            if (!validate) {
                log.error("{} failed to validate custom URL alias", requestId);
                handleCustomAliasValidationFailed(httpRequest, request, startTime);
            }

            final var shortUrlExists = urlDatabaseService.getUrlByShortUrl(request.customAlias());

            if (shortUrlExists.isPresent()) {
                log.error("{} custom alias '{}' is already taken", requestId, request.customAlias());
                handleShortUrlExistsAndThrowException(httpRequest, request, startTime);
            }
        }

        final String shortUrl;

        if (customAlias) {
            shortUrl = request.customAlias();
        } else {
            shortUrl = getShortUrl(request, requestId, httpRequest, startTime);
        }

        final var url = new Url();
        url.setShortUrl(shortUrl);
        url.setUserId(request.userId());
        url.setOriginalUrl(request.originalUrl());
        url.setCreatedAt(System.currentTimeMillis());
        url.setIsCustomAlias(customAlias);
        url.setExpiresAt(expirationTime);

        final var savedUrl = urlDatabaseService.saveUrl(url);

        if (!savedUrl) {
            log.error("[{}]: Shortening URL failed: {}", requestId, request);
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        if (customAlias) {
            customAliasService.updateCustomAlias(requestId, url);
        }

        generateStatisticsEvent(httpRequest, url, EventType.URL_CREATE_SUCCESS, startTime);

        log.info("[{}]: Url shorten successfully: {}", requestId, url);
        final var shortUrlWithDomain = domain + url.getShortUrl();
        log.info("[{}]: Short URL for {} is {}", requestId, request.originalUrl(), shortUrl);

        return new GenerateUrlResponse(shortUrlWithDomain, request.originalUrl(), 201);
    }

    private String getShortUrl(
            final ShortUrlRequest request,
            final Object requestId,
            final HttpServletRequest httpRequest,
            final long startTime
    ) {
        final var shortUrlNumber = numberGeneratorService.generateNextNumber();
        log.debug("[{}]: Number generated for '{}' is {}", requestId, request.originalUrl(), shortUrlNumber);

        if (shortUrlNumber <= 0) {
            log.error("[{}]: Failed to generate number for {}", requestId, request);
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        final var shortUrl = encoderService.encode(shortUrlNumber);
        log.debug("[{}]: Encoded string for {} is {}", requestId, shortUrlNumber, shortUrl);
        return shortUrl;
    }

    private void handleCustomAliasValidationFailed(
            final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime
    ) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"CUSTOM_ALIAS_LIMIT_EXCEEDED"}, HttpStatus.FORBIDDEN.value(), "You have exceeded the custom alias limit as per your subscription plan");
    }

    private void handleShortUrlExistsAndThrowException(
            final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime
    ) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"CUSTOM_ALIAS_EXISTS"}, HttpStatus.CONFLICT.value(), "Custom url alias is already taken");
    }

    public URI getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        final var requestId = extractRequestIdFromRequest(httpRequest);
        log.info("[{}]: Received request to get original url for {}", requestId, shortUrl);

        final var startTime = System.currentTimeMillis();
        final var urlMetadata = urlDatabaseService.getUrlByShortUrl(shortUrl);

        log.debug("[{}]: Metadata fetched for {} is {}", requestId, shortUrl, urlMetadata.orElse(null));

        if (urlMetadata.isEmpty()) {
            log.error("[{}]: Failed to retrieve original URL for {}", requestId, shortUrl);
            handleUrlFindFailureAndThrowException(httpRequest, shortUrl, startTime);
            return null;
        }

        if (urlMetadata.get().getExpiresAt() != null && urlMetadata.get().getExpiresAt() <= System.currentTimeMillis()) {
            log.info("{} expired at {}", requestId, urlMetadata.get().getExpiresAt());
            handleUrlFindFailureAndThrowException(httpRequest, shortUrl, startTime);
        }

        final var originalUrl = urlMetadata.get().getOriginalUrl();
        final URI originalUri;

        if (!originalUrl.startsWith("http") && !originalUrl.startsWith("https")) {
            originalUri = URI.create("https://" + originalUrl);
        } else {
            originalUri = URI.create(originalUrl);
        }

        generateStatisticsEvent(httpRequest, urlMetadata.get(), EventType.URL_GET_SUCCESS, startTime);

        log.debug("[{}]: Original URI for {} is {}", requestId, shortUrl, originalUri);

        return originalUri;
    }

    private void handleShorteningFailureAndThrowException(
            final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime
    ) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"Error shortening url: " + request.originalUrl()}, 500, "Internal Server Error");
    }

    private void handleUrlFindFailureAndThrowException(
            final HttpServletRequest httpRequest, final String shortUrl, final long startTime
    ) throws UrlShortenerException {
        generateStatisticsEvent(
                httpRequest,
                Url.fromShortUrl(shortUrl),
                EventType.URL_GET_FAILED,
                startTime
        );
        throw new UrlShortenerException(new String[]{shortUrl + " not found"}, 404, "Requested URL not found");
    }

    private void generateStatisticsEvent(
            final HttpServletRequest httpRequest, final Url url, final EventType eventType, final long startTime
    ) {
        final var eventDuration = System.currentTimeMillis() - startTime;
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var clientIp = extractClientIpFromRequest(httpRequest);

        final var statisticsEvent = new StatisticsEvent(
                requestId,
                eventType,
                url.getShortUrl(),
                url.getOriginalUrl(),
                url.getUserId(),
                clientIp,
                httpRequest.getHeader("USER-AGENT"),
                url.getCreatedAt(),
                eventDuration
        );

        statisticsService.publishEvent(statisticsEvent);
    }

    private String extractClientIpFromRequest(final HttpServletRequest httpRequest) {
        final var xForwardedFor = httpRequest.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            final var ips = xForwardedFor.split(",");

            if (ips.length > 0) {
                return ips[0].trim();
            }
        }

        return httpRequest.getRemoteAddr();
    }

}
