package com.akgarg.urlshortener.utils;

import com.akgarg.urlshortener.exception.BadRequestException;
import org.springframework.validation.BindingResult;

public final class UrlShortenerUtility {

    private UrlShortenerUtility() {
        throw new IllegalStateException("Can't initialize utility class");
    }

    public static void checkValidationResultAndThrowExceptionOnFailedValidation(
            final BindingResult validationResult
    ) {
        if (validationResult.hasFieldErrors()) {
            final String[] errors = validationResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList()
                    .toArray(String[]::new);

            throw new BadRequestException(errors);
        }

    }

}
