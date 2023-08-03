package com.akgarg.urlshortener.exception;

public class BadRequestException extends RuntimeException {

    private final String[] errors;

    public BadRequestException(final String[] errors) {
        this.errors = errors;
    }

    public String[] getErrors() {
        return this.errors;
    }

}
