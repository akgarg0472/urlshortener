package com.akgarg.urlshortener.v1.subscription.cache;

import com.akgarg.urlshortener.v1.subscription.Subscription;

import java.util.Optional;

public interface SubscriptionCache {

    void addSubscription(Subscription subscription);

    Optional<Subscription> getSubscription(String userId);

}
