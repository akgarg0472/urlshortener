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

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String USER_ID_HEADER_NAME = "X-USER-ID";

    private final RestClient.Builder subscriptionServiceRestClientBuilder;
    private final StatisticsService statisticsService;
    private final SubscriptionCache subscriptionCache;
    private final Environment environment;

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
                log.warn("[{}] Short URLs threshold crossed for user. Allowed: {}, consumed: {}", requestId, allowedShortUrls, currentShortUrlUsageForUser);
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
                    .or(() -> fetchActiveSubscriptionFromSubsService(requestId, userId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Subscription> fetchActiveSubscriptionFromSubsService(final String requestId, final String userId) {
        log.info("[{}] Fetching subscription from subscription service", requestId);

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

            log.info("[{}] subscription API response: {}", requestId, subscriptionResponse);

            if (subscriptionResponse == null || subscriptionResponse.statusCode() != 200) {
                log.warn("[{}] subscription API query failed with response code: {}",
                        requestId,
                        subscriptionResponse != null ? subscriptionResponse.statusCode() : "null"
                );
                return Optional.empty();
            }

            return Optional.of(extractSubscription(subscriptionResponse));
        } catch (Exception e) {
            log.error("[{}] error fetching subscription from subscription service", requestId, e);
            return Optional.empty();
        }
    }

    private int extractAllowedCustomAliasesFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var customAlias = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("custom_alias:"))
                    .findFirst();
            return customAlias.map(ca -> Integer.parseInt(ca.substring("custom_alias:".length()).trim())).orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private int extractAllowedShortUrlsFromSubscriptionPack(final SubscriptionPack subscriptionPack) {
        try {
            final var shortUrls = subscriptionPack.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.startsWith("short_url:"))
                    .findFirst();
            return shortUrls.map(shortUrl -> Integer.parseInt(shortUrl.substring("short_url:".length()).trim())).orElse(0);
        } catch (Exception e) {
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
