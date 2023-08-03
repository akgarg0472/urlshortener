package com.akgarg.urlshortener.exception;

public class UrlShortnerException extends RuntimeException {

    final String[] errors;
    final int errorCode;

    public UrlShortnerException(final String[] errors, final int errorCode) {
        this.errors = errors;
        this.errorCode = errorCode;
    }

    public String[] getErrors() {
        return errors;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
