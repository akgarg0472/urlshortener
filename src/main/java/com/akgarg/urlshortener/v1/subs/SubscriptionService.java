package com.akgarg.urlshortener.v1.subs;

import com.akgarg.urlshortener.exception.SubscriptionException;
import com.akgarg.urlshortener.v1.statistics.StatisticsService;
import com.akgarg.urlshortener.v1.subs.cache.SubscriptionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final StatisticsService statisticsService;
    private final SubscriptionCache subscriptionCache;

    public void addSubscription(final String requestId, final SubscriptionEvent subscriptionEvent) {
        log.info("[{}] Adding subscription: {}", requestId, subscriptionEvent);

        final var subscription = subscriptionEvent.getSubscription();
        final var subscriptionId = subscription.get("id").toString();
        final var userId = subscription.get("user_id").toString();
        final var packId = subscription.get("pack_id").toString();
        final var activatedAt = subscription.get("activated_at").toString();
        final var expiresAt = subscription.get("expires_at").toString();

        final var instance = new Subscription();
        instance.setSubscriptionId(subscriptionId);
        instance.setUserId(userId);
        instance.setPackId(packId);
        instance.setActivatedAt(Long.parseLong(activatedAt));
        instance.setExpiresAt(Long.parseLong(expiresAt));

        subscriptionCache.addOrUpdateSubscription(requestId, instance);
    }

    public void addOrUpdateSubscriptionPack(final String requestId, final SubscriptionEvent subscriptionEvent) {
        if (subscriptionEvent.getEventType() == SubscriptionEventType.SUBSCRIPTION_PACK_CREATED) {
            log.info("[{}] Adding subscription pack: {}", requestId, subscriptionEvent);
        } else {
            log.info("[{}] Updating subscription pack: {}", requestId, subscriptionEvent);
        }

        final var subscriptionPack = subscriptionEvent.getSubscriptionPack();
        final var packId = subscriptionPack.get("id").toString();
        final var packPrivileges = List.of(subscriptionPack.get("privileges").toString().split("~"));
        final var defaultPack = subscriptionPack.get("default_pack").toString();

        final var instance = new SubscriptionPack();
        instance.setPackId(packId);
        instance.setPrivileges(packPrivileges);
        instance.setDefaultPack(Boolean.parseBoolean(defaultPack));

        subscriptionCache.addOrUpdateSubscriptionPack(requestId, instance);
    }

    public void deleteSubscriptionPack(final String requestId, final SubscriptionEvent subscriptionEvent) {
        log.info("[{}] Deleting subscription pack: {}", requestId, subscriptionEvent);
        final var subscriptionPack = subscriptionEvent.getSubscriptionPack();
        final var packId = subscriptionPack.get("id").toString();
        subscriptionCache.removeSubscriptionPack(requestId, packId);
    }

    public boolean isUserAllowedToCreateShortUrl(final String requestId, final String userId) {
        log.info("[{}] Checking if user {} is allowed to create short url", requestId, userId);

        try {
            final var subscriptionPack = getSubscriptionPackForUser(requestId, userId);

            if (subscriptionPack.isEmpty()) {
                log.info("[{}] No subscription found for user: {}", requestId, userId);
                return false;
            }

            final var allowedShortUrls = extractAllowedShortUrlsFromSubscriptionPack(subscriptionPack.get());
            final var currentShortUrlUsageForUser = statisticsService.getCurrentShortUrlUsageForUser(
                    requestId,
                    userId,
                    subscriptionPack.get().getActivatedAt(),
                    subscriptionPack.get().getExpiresAt()
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
            final var subscriptionPack = getSubscriptionPackForUser(requestId, userId);

            if (subscriptionPack.isEmpty()) {
                log.info("[{}] No subscription found for user: {}", requestId, userId);
                return false;
            }

            final var allowedCustomAlias = extractAllowedCustomAliasesFromSubscriptionPack(subscriptionPack.get());

            final var currentCustomAliasUsageForUser = statisticsService.getCurrentCustomAliasUsageForUser(
                    requestId,
                    userId,
                    subscriptionPack.get().getActivatedAt(),
                    subscriptionPack.get().getExpiresAt()
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

    private Optional<SubscriptionPack> getSubscriptionPackForUser(final String requestId, final String userId) {
        try {
            final var subscription = subscriptionCache.getSubscription(requestId, userId);
            final boolean isDefaultPack;
            final String packId;

            if (subscription.isEmpty()) {
                log.info("[{}] No subscription found. Fetching default free subscription: {}", requestId, userId);
                final var defaultSubscription = subscriptionCache.getDefaultSubscriptionPack(requestId);
                isDefaultPack = true;

                if (defaultSubscription.isEmpty()) {
                    log.error("[{}] No free subscription found for user: {}", requestId, userId);
                    return Optional.empty();
                }
                packId = defaultSubscription.get().getPackId();
            } else {
                packId = subscription.get().getPackId();
                isDefaultPack = false;
            }

            final var subscriptionPack = subscriptionCache.getSubscriptionPack(requestId, packId);

            if (subscriptionPack.isEmpty()) {
                return Optional.empty();
            }

            subscriptionPack.get().setSubscriptionId(isDefaultPack ? null : subscription.get().getSubscriptionId());
            subscriptionPack.get().setActivatedAt(isDefaultPack ? 0 : subscription.get().getActivatedAt());
            subscriptionPack.get().setExpiresAt(isDefaultPack ? Long.MAX_VALUE : subscription.get().getExpiresAt());

            return subscriptionPack;
        } catch (Exception e) {
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
