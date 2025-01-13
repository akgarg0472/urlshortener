package com.akgarg.urlshortener.response;

import com.akgarg.urlshortener.exception.BadRequestException;
import com.akgarg.urlshortener.exception.StatisticsException;
import com.akgarg.urlshortener.exception.SubscriptionException;
import com.akgarg.urlshortener.exception.UrlShortenerException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static com.akgarg.urlshortener.response.ApiErrorResponse.ApiErrorType.*;

@Getter
public final class ApiErrorResponse {

    private final String[] errors;

    @JsonProperty("status_code")
    private final int errorCode;

    private final String message;

    private ApiErrorResponse(final String[] errors, final int errorCode, String message) {
        this.errors = errors;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ApiErrorResponse badRequestErrorResponse(final BadRequestException e) {
        return new ApiErrorResponse(e.getErrors(), BAD_REQUEST.code(), e.getMessage());
    }

    public static ApiErrorResponse methodNotAllowedErrorResponse(final String message) {
        return new ApiErrorResponse(new String[]{"Request Method not allowed"}, METHOD_NOT_ALLOWED.code, message);
    }

    public static ApiErrorResponse badRequestErrorResponse(final String message) {
        return new ApiErrorResponse(new String[]{message}, BAD_REQUEST.code, message);
    }

    public static ApiErrorResponse parseException(final UrlShortenerException e) {
        return new ApiErrorResponse(e.getErrors(), e.getErrorCode(), e.getMessage());
    }

    public static ApiErrorResponse parseException(final SubscriptionException e) {
        return new ApiErrorResponse(null, e.getStatusCode().value(), e.getResponseMessage());
    }

    public static ApiErrorResponse parseException(final StatisticsException e) {
        return new ApiErrorResponse(null, e.getStatusCode(), e.getMessage());
    }

    public static ApiErrorResponse internalServerErrorResponse() {
        return new ApiErrorResponse(new String[]{"Internal Server Error"}, INTERNAL_SERVER_ERROR.code, "Internal server error");
    }

    @SuppressWarnings("unused")
    public String[] getErrors() {
        return errors;
    }

    public enum ApiErrorType {

        BAD_REQUEST(400),
        INTERNAL_SERVER_ERROR(500),
        METHOD_NOT_ALLOWED(405),
        NOT_FOUND(404);

        private final int code;

        ApiErrorType(final int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

}
