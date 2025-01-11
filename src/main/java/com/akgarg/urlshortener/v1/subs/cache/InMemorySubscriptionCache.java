package com.akgarg.urlshortener.v1.subs.cache;

import com.akgarg.urlshortener.v1.subs.Subscription;
import com.akgarg.urlshortener.v1.subs.SubscriptionPack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Profile({"dev", "DEV"})
@SuppressWarnings("LoggingSimilarMessage")
public class InMemorySubscriptionCache implements SubscriptionCache {

    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private final Map<String, SubscriptionPack> subscriptionPacks = new HashMap<>();

    @Override
    public void addOrUpdateSubscription(final String requestId, final Subscription subscription) {
        log.info("[{}] Adding subscription {}", requestId, subscription);
        subscriptions.put(subscription.getUserId(), subscription);
    }

    @Override
    public void addOrUpdateSubscriptionPack(final String requestId, final SubscriptionPack pack) {
        log.info("[{}] Adding/Updating subscription pack {}", requestId, pack);
        subscriptionPacks.put(pack.getPackId(), pack);
    }

    @Override
    public void removeSubscriptionPack(final String requestId, final String packId) {
        log.info("[]{} Removing subscription pack {}", requestId, packId);
        subscriptionPacks.remove(packId);
    }

    @Override
    public Optional<Subscription> getSubscription(final String requestId, final String userId) {
        log.info("[{}] Getting subscription {}", requestId, userId);
        return Optional.ofNullable(subscriptions.get(userId));
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPack(final String requestId, final String packId) {
        log.info("[{}] Getting subscription pack for id: {}", requestId, packId);
        return Optional.ofNullable(subscriptionPacks.get(packId));
    }

    @Override
    public Optional<SubscriptionPack> getDefaultSubscriptionPack(final String requestId) {
        return subscriptionPacks.values()
                .stream()
                .filter(SubscriptionPack::isDefaultPack)
                .findFirst();
    }

}
