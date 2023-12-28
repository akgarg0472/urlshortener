package com.akgarg.urlshortener.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String[] errors;
    private final String message;

    public BadRequestException(final String[] errors, String message) {
        this.errors = errors;
        this.message = message;
    }

}
