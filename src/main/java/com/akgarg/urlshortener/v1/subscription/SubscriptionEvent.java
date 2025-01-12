package com.akgarg.urlshortener.v1.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class SubscriptionEvent {

    private SubscriptionEventType eventType;
    private Map<String, Object> subscription;
    private Map<String, Object> subscriptionPack;

}
