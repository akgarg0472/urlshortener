package com.akgarg.urlshortener.request;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public record ShortUrlRequest(
        @NotBlank(message = "userId should be valid") String userId,
        @NotBlank(message = "original url should be valid") String originalUrl
) {
}