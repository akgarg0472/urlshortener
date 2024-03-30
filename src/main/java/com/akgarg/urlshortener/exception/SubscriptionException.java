package com.akgarg.urlshortener.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public class SubscriptionException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String responseMessage;

}
