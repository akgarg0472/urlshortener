package com.akgarg.urlshortener.v1.subscription.cache;

import com.akgarg.urlshortener.v1.subscription.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
public class RedisSubscriptionCache implements SubscriptionCache {

    private static final String REDIS_SUBSCRIPTION_CACHE_PREFIX = "url:shortener:subscription:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void addSubscription(final String requestId, final Subscription subscription) {
        log.info("[{}] Adding subscription to cache", requestId);

        try {
            redisTemplate.opsForValue().set(
                    createSubscriptionKey(subscription.getUserId()),
                    objectMapper.writeValueAsString(subscription)
            );
            log.debug("[{}] Successfully added subscription to cache", requestId);
        } catch (Exception e) {
            log.error("[{}] Error adding subscription to cache", requestId, e);
        }
    }

    @Override
    public Optional<Subscription> getSubscription(final String requestId, final String userId) {
        log.info("[{}] Getting subscription for userId {}", requestId, userId);

        try {
            final var object = redisTemplate.opsForValue().get(createSubscriptionKey(userId));
            if (object == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(object, Subscription.class));
        } catch (Exception e) {
            log.error("[{}] Error retrieving subscription for userId {}", requestId, userId, e);
            return Optional.empty();
        }
    }

    private String createSubscriptionKey(final String userId) {
        return REDIS_SUBSCRIPTION_CACHE_PREFIX + userId;
    }

}
