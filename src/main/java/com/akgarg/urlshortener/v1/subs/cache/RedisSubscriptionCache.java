package com.akgarg.urlshortener.v1.subs.cache;

import com.akgarg.urlshortener.exception.SubscriptionCacheException;
import com.akgarg.urlshortener.v1.subs.Subscription;
import com.akgarg.urlshortener.v1.subs.SubscriptionPack;
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
@Profile({"prod", "PROD"})
public class RedisSubscriptionCache implements SubscriptionCache {

    private static final String REDIS_SUBSCRIPTION_CACHE_PREFIX = "subscription:";
    private static final String REDIS_SUBSCRIPTION_PACK_CACHE_PREFIX = "subscription:pack:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void addOrUpdateSubscription(final String requestId, final Subscription subscription) {
        log.info("[{}] Adding subscription", requestId);
        try {
            redisTemplate.opsForValue().set(createSubscriptionKey(subscription.getUserId()), objectMapper.writeValueAsString(subscription));
        } catch (Exception e) {
            throw new SubscriptionCacheException("Failed to add subscription", e);
        }
    }

    @Override
    public void addOrUpdateSubscriptionPack(final String requestId, final SubscriptionPack pack) {
        log.info("[{}] Adding subscription pack", requestId);
        try {
            redisTemplate.opsForValue().set(createSubscriptionPackKey(pack.getPackId()), objectMapper.writeValueAsString(pack));
        } catch (Exception e) {
            throw new SubscriptionCacheException("Failed to add/update subscription pack", e);
        }
    }

    @Override
    public void removeSubscriptionPack(final String requestId, final String packId) {
        log.info("[{}] Removing subscription pack", requestId);
        redisTemplate.delete(createSubscriptionPackKey(packId));
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
            throw new SubscriptionCacheException("Failed to get subscription for userId: " + userId, e);
        }
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPack(final String requestId, final String packId) {
        log.info("[{}] Getting subscription pack for packId {}", requestId, packId);

        try {
            final var object = redisTemplate.opsForValue().get(createSubscriptionPackKey(packId));
            if (object == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(object, SubscriptionPack.class));
        } catch (Exception e) {
            throw new SubscriptionCacheException("Failed to get subscription pack for packId: " + packId, e);
        }
    }

    @Override
    public Optional<SubscriptionPack> getDefaultSubscriptionPack(final String requestId) {
        // TODO: implement
        return Optional.empty();
    }

    private String createSubscriptionKey(final String userId) {
        return REDIS_SUBSCRIPTION_CACHE_PREFIX + userId;
    }

    private String createSubscriptionPackKey(final String packId) {
        return REDIS_SUBSCRIPTION_PACK_CACHE_PREFIX + packId;
    }

}
