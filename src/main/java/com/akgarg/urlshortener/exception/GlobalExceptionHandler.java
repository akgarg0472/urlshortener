package com.akgarg.urlshortener.exception;

import com.akgarg.urlshortener.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final HttpHeaders methodNotAllowedHeaders = new HttpHeaders();
    private final HttpHeaders redirectHeaders = new HttpHeaders();

    public GlobalExceptionHandler(final Environment environment) {
        methodNotAllowedHeaders.add(HttpHeaders.ALLOW, HttpMethod.GET.name());

        final var isProd = java.util.Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") ||
                        profile.equalsIgnoreCase("production"));
        var location = environment.getProperty("url.shortener.ui.domain", "https://ui.cmpct.xyz/");
        if (!location.matches("^(http://|https://).*")) {
            //noinspection HttpUrlsUsage
            location = isProd ? "https://" + location : "http://" + location;
        }
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
    public ResponseEntity<Object> handleGenericException(final Exception e) {
        if (log.isDebugEnabled()) {
            log.error("Handling exception", e);
        }

        if (e instanceof NoResourceFoundException resourceFoundException) {
            if (resourceFoundException.getHttpMethod().equals(HttpMethod.GET)) {
                return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
            } else {
                return new ResponseEntity<>(methodNotAllowedHeaders, HttpStatus.METHOD_NOT_ALLOWED);
            }
        }

        final ApiErrorResponse errorResponse = switch (e) {
            case HttpRequestMethodNotSupportedException ex ->
                    methodNotAllowedErrorResponse("Request HTTP method '" + ex.getMethod() + "' is not allowed. Allowed: " + Arrays.toString(ex.getSupportedMethods()));
            case HttpMediaTypeNotSupportedException ex ->
                    badRequestErrorResponse("Media type " + ex.getContentType() + " is not supported");
            case HttpMessageNotReadableException ignored ->
                    badRequestErrorResponse("Please provide valid request body");
            case null, default -> internalServerErrorResponse();
        };

        return ResponseEntity.status(errorResponse.getErrorCode())
                .body(errorResponse);
    }

}
