package com.akgarg.urlshortener.v1.subscription;

import com.akgarg.urlshortener.exception.SubscriptionException;
import com.akgarg.urlshortener.v1.statistics.StatisticsService;
import com.akgarg.urlshortener.v1.subscription.cache.SubscriptionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.REQUEST_ID_HEADER;
import static com.akgarg.urlshortener.utils.UrlShortenerUtil.USER_ID_HEADER_NAME;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final RestClient.Builder subscriptionServiceRestClientBuilder;
    private final StatisticsService statisticsService;
    private final SubscriptionCache subscriptionCache;
    private final Environment environment;

    public SubscriptionResponse isUserAllowedToCreateShortUrl(final String requestId, final String userId) {
        log.info("Checking if userId {} is allowed to create short url", userId);

        try {
            final var subscription = getUserActiveSubscription(requestId, userId);

            if (log.isDebugEnabled()) {
                log.debug("Active subscription: {}", subscription.orElse(null));
            }

            if (subscription.isEmpty()) {
                log.info("No subscription details found for userId {}", userId);
                return new SubscriptionResponse(false, false);
            }

            final var allowedShortUrls = extractAllowedShortUrlsFromSubscriptionPack(subscription.get().getPack());
            final var currentShortUrlUsageForUser = statisticsService.getCurrentShortUrlUsageForUser(
                    requestId,
                    userId,
                    subscription.get().getActivatedAt(),
                    subscription.get().getExpiresAt()
            );

            if (currentShortUrlUsageForUser >= allowedShortUrls) {
                log.info("Short URLs threshold crossed for user. Allowed: {}, consumed: {}", allowedShortUrls, currentShortUrlUsageForUser);
                return new SubscriptionResponse(true, false);
            }

            return new SubscriptionResponse(true, true);
        } catch (Exception e) {
            log.error("Error checking if user {} is allowed to create short url", userId);
            throw new SubscriptionException(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "Failed to process request. Please try again later.");
        }
    }

    public SubscriptionResponse isUserAllowedToCreateCustomAlias(final String requestId, final String userId) {
        log.info("Checking if userId {} is allowed to create custom alias", userId);

        try {
            final var subscription = getUserActiveSubscription(requestId, userId);

            if (log.isDebugEnabled()) {
                log.debug("Active subscription: {}", subscription.orElse(null));
            }

            if (subscription.isEmpty()) {
                log.info("No subscription found for userId {}", userId);
                return new SubscriptionResponse(false, false);
            }

            final var allowedCustomAlias = extractAllowedCustomAliasesFromSubscriptionPack(subscription.get().getPack());

            if (log.isDebugEnabled()) {
                log.debug("Allowed custom aliases: {}", allowedCustomAlias);
            }

            final var currentCustomAliasUsageForUser = statisticsService.getCurrentCustomAliasUsageForUser(
                    requestId,
                    userId,
                    subscription.get().getActivatedAt(),
                    subscription.get().getExpiresAt()
            );

            if (currentCustomAliasUsageForUser >= allowedCustomAlias) {
                log.warn("Custom aliases are not allowed for userId {}", userId);
                return new SubscriptionResponse(true, false);
            }

            return new SubscriptionResponse(true, true);
        } catch (Exception e) {
            log.error("Error checking if custom aliases are allowed {}", userId);
            throw new SubscriptionException(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "Failed to process request. Please try again later.");
        }
    }

    private Optional<Subscription> getUserActiveSubscription(final String requestId, final String userId) {
        try {
            return subscriptionCache.getSubscription(userId)
                    .or(() -> {
                        final var subscription = fetchActiveSubscriptionFromSubsService(requestId, userId);
                        subscription.ifPresent(subscriptionCache::addSubscription);
                        return subscription;
                    });
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Subscription> fetchActiveSubscriptionFromSubsService(final String requestId, final String userId) {
        log.info("Fetching subscription from subscription service for userId {}", userId);

        try {
            final var path = environment.getProperty("subscription.service.active.base-path", "/api/v1/subscriptions/active");
            final var subscriptionResponse = subscriptionServiceRestClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("userId", userId)
                            .build()
                    )
                    .header(REQUEST_ID_HEADER, requestId)
                    .header(USER_ID_HEADER_NAME, userId)
                    .retrieve()
                    .toEntity(SubscriptionApiResponse.class)
                    .getBody();

            if (log.isInfoEnabled()) {
                log.info("Subscription API response: {}", subscriptionResponse);
            }

            if (subscriptionResponse == null || subscriptionResponse.statusCode() != 200) {
                log.warn("Subscription API query failed with response code {}",
                        subscriptionResponse != null ? subscriptionResponse.statusCode() : null
                );
                return Optional.empty();
            }

            if (subscriptionResponse.subscriptionDto() == null || subscriptionResponse.subscriptionPackDto() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No subscription received from subscription service for userId {}", userId);
                }
                return Optional.empty();
            }

            return Optional.of(extractSubscription(subscriptionResponse));
        } catch (Exception e) {
            log.error("Error fetching subscription from subscription service", e);
            return Optional.empty();
        }
    }

    private int extractAllowedCustomAliasesFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var customAlias = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("custom_alias:"))
                    .findFirst();
            if (customAlias.isPresent() && customAlias.get().contains("unlimited")) {
                return Integer.MAX_VALUE;
            }
            return customAlias.map(ca -> Integer.parseInt(ca.substring("custom_alias:".length()).trim())).orElse(0);
        } catch (Exception e) {
            log.error("Error extracting allowed custom aliases from subscription pack", e);
            return 0;
        }
    }

    private int extractAllowedShortUrlsFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var shortUrls = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("short_url:"))
                    .findFirst();
            if (shortUrls.isPresent() && shortUrls.get().contains("unlimited")) {
                return Integer.MAX_VALUE;
            }
            return shortUrls.map(shortUrl -> Integer.parseInt(shortUrl.substring("short_url:".length()).trim())).orElse(0);
        } catch (Exception e) {
            log.error("Error extracting allowed short urls from subscription pack", e);
            return 0;
        }
    }

    private Subscription extractSubscription(final SubscriptionApiResponse subscriptionResponse) {
        final var subscription = new Subscription();
        subscription.setSubscriptionId(subscriptionResponse.subscriptionDto().getSubscriptionId());
        subscription.setUserId(subscriptionResponse.subscriptionDto().getUserId());
        subscription.setActivatedAt(subscriptionResponse.subscriptionDto().getActivatedAt());
        subscription.setExpiresAt(subscriptionResponse.subscriptionDto().getExpiresAt());
        subscription.setPack(extractSubscriptionPack(subscriptionResponse.subscriptionPackDto()));
        return subscription;
    }

    private SubscriptionPack extractSubscriptionPack(final SubscriptionPackDto subscriptionPackDto) {
        final var pack = new SubscriptionPack();
        pack.setId(subscriptionPackDto.getId());
        pack.setName(subscriptionPackDto.getName());
        pack.setPrivileges(subscriptionPackDto.getPrivileges());
        pack.setFeatures(subscriptionPackDto.getFeatures());
        pack.setDefaultPack(subscriptionPackDto.isDefaultPack());
        return pack;
    }

}
