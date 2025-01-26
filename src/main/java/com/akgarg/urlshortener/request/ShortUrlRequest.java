package com.akgarg.urlshortener.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShortUrlRequest(

        @JsonProperty("user_id")
        @NotBlank(message = "user_id should be valid")
        String userId,

        @JsonProperty("original_url")
        @NotBlank(message = "original_url should be valid")
        @Pattern(
                regexp = "^(https?://)?[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,})?(:\\d+)?(/.*)?$",
                message = "original_url should be a valid URL"
        )
        String originalUrl,

        @JsonProperty("custom_alias")
        String customAlias,

        @JsonProperty("expires_at")
        Long expiresAt

) {
}