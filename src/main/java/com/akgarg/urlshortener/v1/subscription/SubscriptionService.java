package com.akgarg.urlshortener.v1.subscription;

import com.akgarg.urlshortener.exception.SubscriptionException;
import com.akgarg.urlshortener.v1.statistics.StatisticsService;
import com.akgarg.urlshortener.v1.subscription.cache.SubscriptionCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final RestClient.Builder subscriptionServiceRestClientBuilder;
    private final StatisticsService statisticsService;
    private final SubscriptionCache subscriptionCache;

    @PostConstruct
    public void init() {
        // TODO: initialize cache from subscription service
    }

    public boolean isUserAllowedToCreateShortUrl(final String requestId, final String userId) {
        log.info("[{}] Checking if user {} is allowed to create short url", requestId, userId);

        try {
            final var subscription = getUserSubscription(requestId, userId);

            if (subscription.isEmpty()) {
                log.info("[{}] No subscription found for user: {}", requestId, userId);
                return false;
            }

            final var allowedShortUrls = extractAllowedShortUrlsFromSubscriptionPack(subscription.get().getPack());
            final var currentShortUrlUsageForUser = statisticsService.getCurrentShortUrlUsageForUser(
                    requestId,
                    userId,
                    subscription.get().getActivatedAt(),
                    subscription.get().getExpiresAt()
            );

            if (currentShortUrlUsageForUser >= allowedShortUrls) {
                log.warn("[{}] Custom aliases are not allowed for user: {}", requestId, userId);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("[{}] Error checking if user {} is allowed to create short url", requestId, userId);
            throw new SubscriptionException(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "Failed to check if user " + userId + " is allowed to create short url");
        }
    }

    public boolean isUserAllowedToCreateCustomAlias(final String requestId, final String userId) {
        log.info("[{}] Checking if custom aliases are allowed for user: {}", requestId, userId);

        try {
            final var subscription = getUserSubscription(requestId, userId);

            if (subscription.isEmpty()) {
                log.info("[{}] No subscription found for user: {}", requestId, userId);
                return false;
            }

            final var allowedCustomAlias = extractAllowedCustomAliasesFromSubscriptionPack(subscription.get().getPack());

            final var currentCustomAliasUsageForUser = statisticsService.getCurrentCustomAliasUsageForUser(
                    requestId,
                    userId,
                    subscription.get().getActivatedAt(),
                    subscription.get().getExpiresAt()
            );

            if (currentCustomAliasUsageForUser >= allowedCustomAlias) {
                log.warn("[{}] Custom aliases are not allowed for user: {}", requestId, userId);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("[{}] error checking if custom aliases are allowed: {}", requestId, userId);
            throw new SubscriptionException(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Failed to check custom aliases");
        }
    }

    private Optional<Subscription> getUserSubscription(final String requestId, final String userId) {
        try {
            return subscriptionCache.getSubscription(requestId, userId)
                    .or(() -> fetchSubscriptionFromSubsService(requestId, userId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Subscription> fetchSubscriptionFromSubsService(final String requestId, final String userId) {
        log.info("[{}] Fetching subscription from subscription service", requestId);

        try {
            final var subscriptionResponse = subscriptionServiceRestClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder.queryParam("userId", userId).build())
                    .retrieve()
                    .toEntity(Subscription.class)
                    .getBody();

            log.info("[{}] subscription API response: {}", requestId, subscriptionResponse);

            if (subscriptionResponse == null || subscriptionResponse.getStatusCode() != 200) {
                log.warn("[{}] subscription API query failed with response code: {}",
                        requestId,
                        subscriptionResponse != null ? subscriptionResponse.getStatusCode() : "null"
                );
                return Optional.empty();
            }

            return Optional.of(subscriptionResponse);
        } catch (Exception e) {
            log.error("[{}] error fetching subscription from subscription service", requestId, e);
            return Optional.empty();
        }
    }

    private int extractAllowedCustomAliasesFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var customAlias = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("custom_alias_"))
                    .findFirst();
            return customAlias.map(ca -> Integer.parseInt(ca.substring("custom_alias_".length()).trim())).orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private int extractAllowedShortUrlsFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var shortUrls = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("short_url_"))
                    .findFirst();
            return shortUrls.map(shortUrl -> Integer.parseInt(shortUrl.substring("short_url_".length()).trim())).orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

}
