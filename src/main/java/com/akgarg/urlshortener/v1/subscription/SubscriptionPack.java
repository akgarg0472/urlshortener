package com.akgarg.urlshortener.v1.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubscriptionPack {

    private String packId;
    private String subscriptionId;
    private List<String> privileges;
    private boolean defaultPack;
    private long activatedAt;
    private long expiresAt;

}
