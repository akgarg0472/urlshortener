package com.akgarg.urlshortener.v1.subscription.cache;

import com.akgarg.urlshortener.v1.subscription.Subscription;
import com.akgarg.urlshortener.v1.subscription.SubscriptionPack;

import java.util.Optional;

public interface SubscriptionCache {

    void addOrUpdateSubscription(String requestId, Subscription subscription);

    void addOrUpdateSubscriptionPack(String requestId, SubscriptionPack pack);

    void removeSubscriptionPack(String requestId, String packId);

    Optional<Subscription> getSubscription(String requestId, String userId);

    Optional<SubscriptionPack> getSubscriptionPack(String requestId, String packId);

    Optional<SubscriptionPack> getDefaultSubscriptionPack(String requestId);

}
