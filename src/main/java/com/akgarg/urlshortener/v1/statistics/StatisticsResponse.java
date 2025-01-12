package com.akgarg.urlshortener.v1.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatisticsResponse(
        @JsonProperty("status_code") int statusCode,
        String message,
        String key,
        int value
) {
}
