package com.akgarg.urlshortener.exception;

import lombok.Getter;

@Getter
public class UrlShortenerException extends RuntimeException {

    private final String[] errors;
    private final int errorCode;
    private final String message;

    public UrlShortenerException(final String[] errors, final int errorCode, String message) {
        this.errors = errors;
        this.errorCode = errorCode;
        this.message = message;
    }

}
