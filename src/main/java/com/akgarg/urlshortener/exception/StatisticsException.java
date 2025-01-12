package com.akgarg.urlshortener.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsException extends RuntimeException {

    private final int statusCode;
    private final String message;

}
