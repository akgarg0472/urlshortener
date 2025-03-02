package com.akgarg.urlshortener.v1.api;

import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.events.EventType;
import com.akgarg.urlshortener.events.StatisticsEvent;
import com.akgarg.urlshortener.events.StatisticsEventService;
import com.akgarg.urlshortener.exception.UrlShortenerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.ApiErrorResponse;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
import com.akgarg.urlshortener.response.UrlResponse;
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
@SuppressWarnings("DuplicatedCode")
public class UrlService {

    private final StatisticsEventService statisticsEventService;
    private final NumberGeneratorService numberGeneratorService;
    private final SubscriptionService subscriptionService;
    private final UrlDatabaseService urlDatabaseService;
    private final EncoderService encoderService;

    public UrlResponse generateShortUrl(final HttpServletRequest httpRequest, final ShortUrlRequest request) {
        final var requestId = extractRequestIdFromRequest(httpRequest);
        final var startTime = System.currentTimeMillis();

        log.info("Received generate short url request: {}", request);

        final var responseBuilder = GenerateUrlResponse.builder();

        final long expirationTime;

        if (request.expiresAt() != null) {
            expirationTime = request.expiresAt();
        } else {
            expirationTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 100L);
        }

        if (expirationTime < System.currentTimeMillis()) {
            log.info("Invalid expiration time for generate short url request");
            final var response = ApiErrorResponse.builder()
                    .statusCode(400)
                    .errors(new String[]{"Invalid expiration time"})
                    .message("URL Expiration time is past date: " + expirationTime)
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
        }

        final var userIdFromRequest = extractUserIdFromRequest(httpRequest);

        if (userIdFromRequest == null || !userIdFromRequest.equals(request.userId())) {
            log.info("Invalid user id provided in generate short url request");
            final var response = ApiErrorResponse.builder()
                    .statusCode(400)
                    .errors(new String[]{"User id in header and request body mismatch"})
                    .message("Invalid user id")
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
        }

        final var userAllowedToCreateShortUrl = subscriptionService.isUserAllowedToCreateShortUrl(requestId, userIdFromRequest);

        if (!userAllowedToCreateShortUrl.subscriptionFound()) {
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
            final var response = ApiErrorResponse.builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .errors(new String[]{"Subscription not found"})
                    .message("Failed to create short url")
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
        }

        if (!userAllowedToCreateShortUrl.actionAllowed()) {
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
            final var response = ApiErrorResponse.builder()
                    .statusCode(HttpStatus.FORBIDDEN.value())
                    .errors(new String[]{"Short url limit reached"})
                    .message("You have exceeded the short url limit as per your subscription plan")
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
        }

        final var customAlias = request.customAlias() != null && !request.customAlias().isBlank();

        if (customAlias) {
            final var userAllowedToCreateCustomAlias = subscriptionService.isUserAllowedToCreateCustomAlias(requestId, userIdFromRequest);

            if (!userAllowedToCreateCustomAlias.subscriptionFound()) {
                generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
                final var response = ApiErrorResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .errors(new String[]{"Subscription not found"})
                        .message("Failed to create short url")
                        .build();
                return new UrlResponse(response.getStatusCode(), response);
            }

            if (!userAllowedToCreateCustomAlias.actionAllowed()) {
                generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
                final var response = ApiErrorResponse.builder()
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .errors(new String[]{"Custom alias limit reached"})
                        .message("You have exceeded the custom alias limit as per your subscription plan")
                        .build();
                return new UrlResponse(response.getStatusCode(), response);
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
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
            final var response = ApiErrorResponse.internalServerErrorResponse();
            return new UrlResponse(response.getStatusCode(), response);
        }

        generateStatisticsEvent(httpRequest, url, EventType.URL_CREATE_SUCCESS, startTime);

        log.info("Url shorten successfully: {}", url);
        log.info("Short URL for {} is {}", request.originalUrl(), shortUrl);

        final var response = responseBuilder
                .shortUrl(url.getShortUrl())
                .originalUrl(request.originalUrl())
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return new UrlResponse(response.getStatusCode(), response);
    }

    public UrlResponse getOriginalUrl(final HttpServletRequest httpRequest, final String shortUrl) {
        log.info("Received request to get original url for '{}'", shortUrl);

        final var startTime = System.currentTimeMillis();
        final var urlMetadata = urlDatabaseService.getUrlByShortUrl(shortUrl);

        if (log.isDebugEnabled()) {
            log.debug("Metadata fetched for {}: {}", shortUrl, urlMetadata.orElse(null));
        }

        if (urlMetadata.isEmpty()) {
            log.info("Original URL not found for '{}'", shortUrl);
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(shortUrl), EventType.URL_GET_FAILED, startTime);
            final var response = ApiErrorResponse.builder()
                    .message("Requested URL not found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
        }

        if (urlMetadata.get().getExpiresAt() != null && urlMetadata.get().getExpiresAt() <= System.currentTimeMillis()) {
            if (log.isDebugEnabled()) {
                log.debug("URL expired at {}", urlMetadata.get().getExpiresAt());
            }
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(shortUrl), EventType.URL_GET_FAILED, startTime);
            final var response = ApiErrorResponse.builder()
                    .message("Requested URL is expired")
                    .statusCode(HttpStatus.GONE.value())
                    .build();
            return new UrlResponse(response.getStatusCode(), response);
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

        return new UrlResponse(HttpStatus.OK.value(), originalUri);
    }

    private String getShortUrl(final ShortUrlRequest request, final HttpServletRequest httpRequest, final long startTime) {
        final var shortUrlNumber = numberGeneratorService.generateNextNumber();

        if (log.isDebugEnabled()) {
            log.debug("Number generated for '{}' is {}", request.originalUrl(), shortUrlNumber);
        }

        if (shortUrlNumber <= 0) {
            log.error("Failed to generate unique number");
            generateStatisticsEvent(httpRequest, Url.fromShortUrl(request.originalUrl()), EventType.URL_CREATE_FAILED, startTime);
            throw new UrlShortenerException(new String[]{"Failed to process request"}, 500, "Internal Server Error");
        }

        final var shortUrl = encoderService.encode(shortUrlNumber);

        if (log.isDebugEnabled()) {
            log.debug("Encoded string for {} is {}", shortUrlNumber, shortUrl);
        }

        return shortUrl;
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
