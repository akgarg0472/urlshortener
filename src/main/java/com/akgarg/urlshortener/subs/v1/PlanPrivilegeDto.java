package com.akgarg.urlshortener.subs.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

record PlanPrivilegeDto(
        @JsonProperty("id")
        int id,
        @JsonProperty("privilege")
        String name
) {
}