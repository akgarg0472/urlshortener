package com.akgarg.urlshortener.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Subscription {

    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("subscription_id")
    private String subscriptionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("activated_at")
    private long activatedAt;

    @JsonProperty("expires_at")
    private long expiresAt;

    @JsonProperty("pack")
    private SubscriptionPack pack;

}
