package com.akgarg.urlshortener.subs.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record SubscriptionDTO(

        @JsonProperty("subscription_id")
        String id,

        @JsonProperty("plan_id")
        String planId,

        @JsonProperty("amount")
        Long amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("privileges")
        List<PlanPrivilegeDto> privileges,

        @JsonProperty("subscribed_at")
        long subscribedAt,

        @JsonProperty("expiring_at")
        long expiresAt
) {
}
