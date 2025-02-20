package com.akgarg.urlshortener.utils;

import com.akgarg.urlshortener.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

public final class UrlShortenerUtil {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String USER_ID_HEADER_NAME = "X-USER-ID";
    private static final String X_FORWARDED_FOR_HEADER_NAME = "X-Forwarded-For";
    private static final String USER_AGENT_HEADER_NAME = "USER-AGENT";

    private UrlShortenerUtil() {
        throw new IllegalStateException("Can't initialize utility class");
    }

    public static void checkValidationResultAndThrowExceptionOnFailedValidation(
            final BindingResult validationResult
    ) {
        if (validationResult.hasFieldErrors()) {
            final String[] errors = validationResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList()
                    .toArray(String[]::new);

            throw new BadRequestException(errors, "Request Validation Failed");
        }
    }

    public static String extractClientIpFromRequest(final HttpServletRequest httpRequest) {
        final var xForwardedFor = httpRequest.getHeader(X_FORWARDED_FOR_HEADER_NAME);

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            final var ips = xForwardedFor.split(",");

            if (ips.length > 0) {
                return ips[0].trim();
            }
        }

        return httpRequest.getRemoteAddr();
    }

    public static String extractRequestIdFromRequest(final HttpServletRequest httpRequest) {
        final var requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        return requestId != null ? requestId : String.valueOf(System.nanoTime());
    }

    public static String extractUserIdFromRequest(final HttpServletRequest httpRequest) {
        return httpRequest.getHeader(USER_ID_HEADER_NAME);
    }

    public static String extractUserAgentFromRequest(final HttpServletRequest httpRequest) {
        return httpRequest.getHeader(USER_AGENT_HEADER_NAME);
    }

}
