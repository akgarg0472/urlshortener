package com.akgarg.urlshortener.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateUrlResponse(
        @JsonProperty("short_url") String shortUrl,
        @JsonProperty("original_url") String originalUrl,
        @JsonProperty("status_code") int statusCode) {
}
