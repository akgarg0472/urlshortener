package com.akgarg.urlshortener.exception;

import com.akgarg.urlshortener.response.ApiErrorResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;

import static com.akgarg.urlshortener.response.ApiErrorResponse.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final HttpHeaders redirectHeaders;

    public GlobalExceptionHandler(final Environment environment) {
        final var isProd = java.util.Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") ||
                        profile.equalsIgnoreCase("production"));

        var location = environment.getProperty("url.shortener.ui.domain", "https://ui.cmpct.xyz/");

        if (!location.matches("^(http://|https://).*")) {
            //noinspection HttpUrlsUsage
            location = isProd ? "https://" + location : "http://" + location;
        }

        redirectHeaders = new HttpHeaders();
        redirectHeaders.add(HttpHeaders.LOCATION, location);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(final BadRequestException e) {
        return ResponseEntity.badRequest().body(badRequestErrorResponse(e));
    }

    @ExceptionHandler(UrlShortenerException.class)
    public ResponseEntity<ApiErrorResponse> handleUrlShortenerException(final UrlShortenerException e) {
        return ResponseEntity.status(e.getErrorCode()).body(parseException(e));
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionException(final SubscriptionException e) {
        return ResponseEntity.status(e.getStatusCode()).body(parseException(e));
    }

    @ExceptionHandler(StatisticsException.class)
    public ResponseEntity<ApiErrorResponse> handleStatisticsException(final StatisticsException e) {
        return ResponseEntity.status(e.getStatusCode()).body(parseException(e));
    }

    @ExceptionHandler(Exception.class)
    @SuppressWarnings("all")
    public ResponseEntity<?> handleGenericException(final Exception e) {
        if (e instanceof NoResourceFoundException) {
            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
        }

        final ApiErrorResponse errorResponse = switch (e) {
            case HttpRequestMethodNotSupportedException ex ->
                    methodNotAllowedErrorResponse("Request HTTP method '" + ex.getMethod() + "' is not allowed. Allowed: " + Arrays.toString(ex.getSupportedMethods()));
            case HttpMediaTypeNotSupportedException ex ->
                    badRequestErrorResponse("Media type " + ex.getContentType() + " is not supported");
            case HttpMessageNotReadableException ex -> badRequestErrorResponse("Please provide valid request body");
            case null, default -> internalServerErrorResponse();
        };

        return ResponseEntity.status(errorResponse.getErrorCode())
                .body(errorResponse);
    }

}
