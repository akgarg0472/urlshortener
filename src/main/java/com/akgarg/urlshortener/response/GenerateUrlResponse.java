package com.akgarg.urlshortener.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenerateUrlResponse {

    @JsonProperty("short_url")
    private String shortUrl;

    @JsonProperty("original_url")
    private String originalUrl;

    @JsonProperty("status_code")
    private int statusCode;

}
