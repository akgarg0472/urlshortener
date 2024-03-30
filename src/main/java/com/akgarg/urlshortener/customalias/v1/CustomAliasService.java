package com.akgarg.urlshortener.customalias.v1;

import com.akgarg.urlshortener.customalias.v1.db.CustomAliasDatabaseService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.subs.v1.SubscriptionService;
import com.akgarg.urlshortener.url.v1.Url;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomAliasService {

    private static final Logger LOGGER = LogManager.getLogger(CustomAliasService.class);

    private final CustomAliasDatabaseService customAliasDatabaseService;
    private final SubscriptionService subscriptionService;

    public boolean validate(final Object requestId, final ShortUrlRequest request) {
        final Optional<String> customAlias = subscriptionService.getCustomAlias(requestId, request.userId());

        if (customAlias.isEmpty()) {
            LOGGER.info("{} subscription or custom alias not found for user", requestId);
            return false;
        }

        final long customAliasThreshold = subscriptionService.extractCustomAliasThreshold(requestId, customAlias.get());
        final long customAliasSinceTimestamp = subscriptionService.getCustomAliasSinceTimestamp(customAlias.get());
        final long consumedCustomAlias = getConsumedCustomAlias(request.userId(), customAliasSinceTimestamp);

        LOGGER.info("{} allowed custom alias: {}, consumed custom alias: {}", requestId, customAliasThreshold, consumedCustomAlias);

        return customAliasThreshold > consumedCustomAlias;
    }

    private long getConsumedCustomAlias(final String userId, final long timestampSince) {
        return customAliasDatabaseService.getConsumedCustomAliasForUserIdSince(userId, timestampSince);
    }

    public void updateCustomAlias(final Object requestId, final Url url) {
        final var customAlias = new CustomAlias();
        customAlias.setAlias(url.getShortUrl());
        customAlias.setUserId(url.getUserId());
        customAlias.setCreatedAt(System.currentTimeMillis());
        final boolean customAliasSaveResult = customAliasDatabaseService.addCustomAlias(customAlias);
        LOGGER.info("{} custom alias save result: {}", requestId, customAliasSaveResult);
    }

}
