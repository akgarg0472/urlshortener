package com.akgarg.urlshortener.exception;

import com.akgarg.urlshortener.response.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

import static com.akgarg.urlshortener.response.ApiErrorResponse.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(final BadRequestException e) {
        return ResponseEntity.badRequest().body(badRequestErrorResponse(e));
    }

    @ExceptionHandler(UrlShortenerException.class)
    public ResponseEntity<ApiErrorResponse> handleUrlShortenerException(final UrlShortenerException e) {
        return ResponseEntity.status(e.getErrorCode()).body(parseException(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(final Exception e) {
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
