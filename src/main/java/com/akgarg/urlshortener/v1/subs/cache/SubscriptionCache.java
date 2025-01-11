package com.akgarg.urlshortener.v1.subs.cache;

import com.akgarg.urlshortener.v1.subs.Subscription;
import com.akgarg.urlshortener.v1.subs.SubscriptionPack;

import java.util.Optional;

public interface SubscriptionCache {

    void addOrUpdateSubscription(String requestId, Subscription subscription);

    void addOrUpdateSubscriptionPack(String requestId, SubscriptionPack pack);

    void removeSubscriptionPack(String requestId, String packId);

    Optional<Subscription> getSubscription(String requestId, String userId);

    Optional<SubscriptionPack> getSubscriptionPack(String requestId, String packId);

    Optional<SubscriptionPack> getDefaultSubscriptionPack(String requestId);

}
