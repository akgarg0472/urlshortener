package com.akgarg.urlshortener.v1.subscription.cache;

import com.akgarg.urlshortener.v1.subscription.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Profile("dev")
public class InMemorySubscriptionCache implements SubscriptionCache {

    private final Map<String, Subscription> subscriptions = new HashMap<>();

    @Override
    public void addSubscription(final Subscription subscription) {
        if (log.isDebugEnabled()) {
            log.debug("Adding subscription to cache: {}", subscription);
        }
        subscriptions.put(subscription.getUserId(), subscription);
    }

    @Override
    public Optional<Subscription> getSubscription(final String userId) {
        log.info("Getting subscription for userId {}", userId);
        return Optional.ofNullable(subscriptions.get(userId));
    }

}
