package com.akgarg.urlshortener.v1.api;

import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.events.EventType;
import com.akgarg.urlshortener.events.StatisticsEvent;
import com.akgarg.urlshortener.events.StatisticsEventService;
import com.akgarg.urlshortener.exception.UrlShortenerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
import com.akgarg.urlshortener.v1.db.Url;
import com.akgarg.urlshortener.v1.db.UrlDatabaseService;
import com.akgarg.urlshortener.v1.subscription.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final StatisticsEventService statisticsEventService;
    private final NumberGeneratorService numberGeneratorService;
    private final SubscriptionService subscriptionService;
    private final UrlDatabaseService urlDatabaseService;
    private final EncoderService encoderService;

    public GenerateUrlResponse generateShortUrl(final HttpServletRequest httpRequest, final ShortUrlRequest request) {
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var startTime = System.currentTimeMillis();

        log.info("Received generate short url request: {}", request);

        final long expirationTime;

        if (request.expiresAt() != null) {
            expirationTime = request.expiresAt();
        } else {
            expirationTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 100L);
        }

        if (expirationTime < System.currentTimeMillis()) {
            log.info("Invalid expiration time for generate short url request");
            throw new UrlShortenerException(new String[]{"Invalid expiration time"}, 400, "URL Expiration time is past date: " + expirationTime);
        }

        final var userIdFromRequest = extractUserIdFromRequest(httpRequest);

        if (userIdFromRequest == null || !userIdFromRequest.equals(request.userId())) {
            log.error("Invalid user id provided in generate short url request");
            throw new UrlShortenerException(new String[]{"User id in header and request body mismatch"}, 400, "Invalid user id");
        }

        final var userAllowedToCreateShortUrl = subscriptionService.isUserAllowedToCreateShortUrl(requestId, userIdFromRequest);

        if (!userAllowedToCreateShortUrl) {
            handleUserNotAllowedToCreateShortUrlAndThrowException(httpRequest, request, startTime);
        }

        final var customAlias = request.customAlias() != null && !request.customAlias().isBlank();

        if (customAlias) {
            final var userAllowedToCreateCustomAlias = subscriptionService.isUserAllowedToCreateCustomAlias(requestId, userIdFromRequest);

            if (!userAllowedToCreateCustomAlias) {
                handleCustomAliasValidationFailed(httpRequest, request, startTime);
            }
        }

        final String shortUrl = customAlias ? request.customAlias() : getShortUrl(request, httpRequest, startTime);

        final var url = new Url();
        url.setShortUrl(shortUrl);
        url.setUserId(request.userId());
        url.setOriginalUrl(request.originalUrl());
        url.setCreatedAt(System.currentTimeMillis());
        url.setCustomAlias(customAlias);
        url.setExpiresAt(expirationTime);

        final var savedUrl = urlDatabaseService.saveUrl(url);

        if (!savedUrl) {
            log.error("Fail to save short url: {}", shortUrl);
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        generateStatisticsEvent(httpRequest, url, EventType.URL_CREATE_SUCCESS, startTime);

        log.info("Url shorten successfully: {}", url);
        log.info("Short URL for {} is {}", request.originalUrl(), shortUrl);

        return new GenerateUrlResponse(url.getShortUrl(), request.originalUrl(), 201);
    }

    public URI getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        log.info("Received request to get original url for {}", shortUrl);

        final var startTime = System.currentTimeMillis();
        final var urlMetadata = urlDatabaseService.getUrlByShortUrl(shortUrl);

        if (log.isDebugEnabled()) {
            log.debug("Metadata fetched for {} is {}", shortUrl, urlMetadata.orElse(null));
        }

        if (urlMetadata.isEmpty()) {
            log.info("Failed to retrieve original URL for {}", shortUrl);
            handleUrlFindFailureAndThrowException(httpRequest, shortUrl, startTime);
            return null;
        }

        if (urlMetadata.get().getExpiresAt() != null && urlMetadata.get().getExpiresAt() <= System.currentTimeMillis()) {
            if (log.isDebugEnabled()) {
                log.debug("URL expired at {}", urlMetadata.get().getExpiresAt());
            }
            handleUrlExpiredFailureAndThrowException(httpRequest, shortUrl, startTime);
        }

        final var originalUrl = urlMetadata.get().getOriginalUrl();
        final URI originalUri;

        if (!originalUrl.startsWith("http") && !originalUrl.startsWith("https")) {
            originalUri = URI.create("https://" + originalUrl);
        } else {
            originalUri = URI.create(originalUrl);
        }

        generateStatisticsEvent(httpRequest, urlMetadata.get(), EventType.URL_GET_SUCCESS, startTime);

        if (log.isDebugEnabled()) {
            log.debug("Original URI for {} is {}", shortUrl, originalUri);
        }

        return originalUri;
    }

    private String getShortUrl(final ShortUrlRequest request, final HttpServletRequest httpRequest, final long startTime) {
        final var shortUrlNumber = numberGeneratorService.generateNextNumber();

        if (log.isDebugEnabled()) {
            log.debug("Number generated for '{}' is {}", request.originalUrl(), shortUrlNumber);
        }

        if (shortUrlNumber <= 0) {
            log.error("Failed to generate unique number for request");
            handleShorteningFailureAndThrowException(httpRequest, request, startTime);
        }

        final var shortUrl = encoderService.encode(shortUrlNumber);

        if (log.isDebugEnabled()) {
            log.debug("Encoded string for {} is {}", shortUrlNumber, shortUrl);
        }

        return shortUrl;
    }

    private void handleCustomAliasValidationFailed(final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"Custom alias limit reached"}, HttpStatus.FORBIDDEN.value(), "You have exceeded the custom alias limit as per your subscription plan");
    }

    private void handleUserNotAllowedToCreateShortUrlAndThrowException(final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"Short url limit reached"}, HttpStatus.TOO_MANY_REQUESTS.value(), "You have exceeded the short url limit as per your subscription plan");
    }

    private void handleShorteningFailureAndThrowException(final HttpServletRequest httpRequest, final ShortUrlRequest request, final long startTime) {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
        throw new UrlShortenerException(new String[]{"Failed to process request"}, 500, "Internal Server Error");
    }

    private void handleUrlExpiredFailureAndThrowException(final HttpServletRequest httpRequest, final String shortUrl, final long startTime) throws UrlShortenerException {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(shortUrl), EventType.URL_GET_FAILED, startTime);
        throw new UrlShortenerException(new String[]{shortUrl + " is expired"}, 410, "Requested short URL is expired");
    }

    private void handleUrlFindFailureAndThrowException(final HttpServletRequest httpRequest, final String shortUrl, final long startTime) throws UrlShortenerException {
        generateStatisticsEvent(httpRequest, Url.fromShortUrl(shortUrl), EventType.URL_GET_FAILED, startTime);
        throw new UrlShortenerException(new String[]{shortUrl + " not found"}, 404, "Requested URL not found");
    }

    private void generateStatisticsEvent(final HttpServletRequest httpRequest, final Url url, final EventType eventType, final long startTime) {
        final var eventDuration = System.currentTimeMillis() - startTime;
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var clientIp = extractClientIpFromRequest(httpRequest);
        final var userAgent = extractUserAgentFromRequest(httpRequest);

        final var statisticsEvent = new StatisticsEvent(requestId,
                eventType,
                url.getShortUrl(),
                url.getOriginalUrl(),
                url.isCustomAlias(),
                url.getUserId(),
                clientIp,
                userAgent,
                url.getCreatedAt(),
                eventDuration,
                System.currentTimeMillis());

        statisticsEventService.publishEvent(statisticsEvent);
    }

}
