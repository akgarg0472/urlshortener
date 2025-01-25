package com.akgarg.urlshortener.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

record SubscriptionApiResponse(@JsonProperty("status_code") int statusCode,
                               @JsonProperty("message") String message,
                               @JsonProperty("subscription") SubscriptionDto subscriptionDto,
                               @JsonProperty("pack") SubscriptionPackDto subscriptionPackDto) {
}

@Getter
@Setter
@ToString
class SubscriptionDto {

    @JsonProperty("subscription_id")
    private String subscriptionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("activated_at")
    private long activatedAt;

    @JsonProperty("expires_at")
    private long expiresAt;

}

@Getter
@Setter
@ToString
class SubscriptionPackDto {

    @JsonProperty("pack_id")
    private String id;

    @JsonProperty("pack_name")
    private String name;

    @JsonProperty("privileges")
    private List<String> privileges;

    @JsonProperty("features")
    private List<String> features;

    @JsonProperty("default_pack")
    private boolean defaultPack;

}