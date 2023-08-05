package com.akgarg.urlshortener.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.akgarg.urlshortener.exception.ApiErrorResponse.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(final BadRequestException e) {
        return ResponseEntity.badRequest().body(badRequestErrorResponse(e));
    }

    @ExceptionHandler(UrlShortnerException.class)
    public ResponseEntity<ApiErrorResponse> handleUrlShortnerException(final UrlShortnerException e) {
        return ResponseEntity.status(e.getErrorCode()).body(parseException(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(final Exception e) {
        final ApiErrorResponse errorResponse = switch (e.getClass().getSimpleName()) {
            case "HttpRequestMethodNotSupportedException" -> methodNotAllowedErrorResponse();
            case "HttpMediaTypeNotSupportedException" -> badRequestErrorResponse("Media type is not supported");
            case "HttpMessageNotReadableException" -> badRequestErrorResponse("Please provide valid request body");
            default -> internalServerErrorResponse();
        };

        return ResponseEntity.status(errorResponse.getErrorCode())
                .body(errorResponse);
    }

}
