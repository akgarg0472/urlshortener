package com.akgarg.urlshortener.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ShortUrlRequest(

        @JsonProperty("user_id")
        @NotBlank(message = "user_id should be valid")
        String userId,

        @JsonProperty("original_url")
        @NotBlank(message = "original_url should be valid") String originalUrl
) {
}