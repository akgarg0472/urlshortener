package com.akgarg.urlshortener.v1.subs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Subscription {

    private String subscriptionId;
    private String userId;
    private String packId;
    private long expiresAt;

}
