package com.akgarg.urlshortener.v1.subs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubscriptionPack {

    private String packId;
    private List<String> privileges;
    private boolean defaultPack;
    private long expiresAt;

}
