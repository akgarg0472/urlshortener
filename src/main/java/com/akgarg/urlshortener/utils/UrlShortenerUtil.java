package com.akgarg.urlshortener.utils;

import com.akgarg.urlshortener.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

public final class UrlShortenerUtil {

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

    public static Object extractRequestIdFromRequest(final HttpServletRequest httpRequest) {
        final var requestId = httpRequest.getAttribute("requestId");
        return requestId != null ? requestId : System.nanoTime();
    }

}
