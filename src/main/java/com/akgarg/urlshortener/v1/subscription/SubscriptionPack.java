package com.akgarg.urlshortener.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubscriptionPack {

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
