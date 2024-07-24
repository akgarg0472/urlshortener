package com.akgarg.urlshortener.subs.v1;

import com.akgarg.urlshortener.exception.SubscriptionException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Logger LOGGER = LogManager.getLogger(SubscriptionService.class);
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    private final RestClient.Builder restClientBuilder;

    public Optional<String> getCustomAlias(final Object requestId, final String userId) {
        try {
            final var subscriptionResponse = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("subscription-service")
                            .path("/api/v1/subscriptions")
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .toEntity(GetSubscriptionResponse.class);

            final var responseBody = subscriptionResponse.getBody();

            if (responseBody == null) {
                LOGGER.error("{} no response received from subs service", requestId);

                throw new SubscriptionException(
                        HttpStatusCode.valueOf(500),
                        "No response received from subscription"
                );
            }

            final var privileges = responseBody
                    .subscription()
                    .privileges();

            LOGGER.debug(
                    "{} subscription privileges: {}",
                    requestId,
                    privileges.stream().map(PlanPrivilegeDto::name).toList()
            );

            final var customAliasPrivilege = privileges
                    .stream()
                    .filter(this::isCustomAliasPrivilege)
                    .findFirst();

            LOGGER.info("{} custom alias for user subscription: {}", requestId, customAliasPrivilege.orElse(null));

            return customAliasPrivilege.map(PlanPrivilegeDto::name);
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException clientErrorException) {
                LOGGER.error("{} error checking for subscription -> {}", requestId, clientErrorException.getMessage());

                @SuppressWarnings("unchecked") final Map<String, Object> responseBody = clientErrorException.getResponseBodyAs(Map.class);

                final String message;

                if (responseBody != null && responseBody.get("message") != null) {
                    message = responseBody.get("message").toString();
                } else {
                    message = "Subscription not found";
                }

                throw new SubscriptionException(
                        clientErrorException.getStatusCode(),
                        message
                );
            }

            LOGGER.error("{} {} communicating with subscription service: {}", requestId, e.getClass().getSimpleName(), e.getMessage());

            throw e;
        }
    }

    private boolean isCustomAliasPrivilege(final PlanPrivilegeDto privilege) {
        return privilege
                .name()
                .toUpperCase(Locale.ROOT)
                .startsWith(PrivilegeEnums.PrivilegePrefix.CUSTOM_ALIAS.name());
    }

    public long extractCustomAliasThreshold(final Object requestId, final String customAlias) {
        try {
            return Long.parseLong(customAlias.substring(customAlias.lastIndexOf('_') + 1));
        } catch (Exception e) {
            LOGGER.error("{} error extracting allowed custom alias", requestId, e);
            return 0;
        }
    }

    public long getCustomAliasSinceTimestamp(final String customAlias) {
        final PrivilegeEnums.CustomAliasDuration duration = getCustomAliasDuration(customAlias);

        return switch (duration) {
            case DAILY -> LocalDate.now()
                    .atStartOfDay()
                    .atZone(UTC_ZONE_ID)
                    .toInstant()
                    .toEpochMilli();
            case WEEKLY -> LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay()
                    .atZone(UTC_ZONE_ID)
                    .toInstant()
                    .toEpochMilli();
            case MONTHLY -> LocalDate.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .atStartOfDay()
                    .atZone(UTC_ZONE_ID)
                    .toInstant()
                    .toEpochMilli();
        };
    }

    private PrivilegeEnums.CustomAliasDuration getCustomAliasDuration(final String privilege) {
        final var duration = privilege.substring(
                PrivilegeEnums.PrivilegePrefix.CUSTOM_ALIAS.name().length() + 1,
                privilege.lastIndexOf('_')
        );
        return PrivilegeEnums.CustomAliasDuration.valueOf(duration.toUpperCase());
    }

}
