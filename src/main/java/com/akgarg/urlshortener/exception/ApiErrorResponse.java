package com.akgarg.urlshortener.exception;

import static com.akgarg.urlshortener.exception.ApiErrorResponse.ApiErrorType.*;

public final class ApiErrorResponse {

    private final String[] errors;
    private final ApiErrorType errorType;

    private ApiErrorResponse(final String[] errors, final ApiErrorType errorType) {
        this.errors = errors;
        this.errorType = errorType;
    }

    static ApiErrorResponse badRequestErrorResponse(final BadRequestException e) {
        return new ApiErrorResponse(e.getErrors(), BAD_REQUEST);
    }

    static ApiErrorResponse methodNotAllowedErrorResponse() {
        return new ApiErrorResponse(new String[]{"Request Method not allowed"}, METHOD_NOT_ALLOWED);
    }

    static ApiErrorResponse badRequestErrorResponse(final String message) {
        return new ApiErrorResponse(new String[]{message}, BAD_REQUEST);
    }

    static ApiErrorResponse parseException(final UrlShortnerException e) {
        return new ApiErrorResponse(e.getErrors(), getErrorTypeFromErrorCode(e.getErrorCode()));
    }

    private static ApiErrorType getErrorTypeFromErrorCode(final int errorCode) {
        return switch (errorCode) {
            case 404 -> NOT_FOUND;
            case 400 -> BAD_REQUEST;
            default -> INTERNAL_SERVER_ERROR;
        };
    }

    static ApiErrorResponse internalServerErrorResponse() {
        return new ApiErrorResponse(new String[]{"Internal Server Error"}, INTERNAL_SERVER_ERROR);
    }

    public ApiErrorType getErrorType() {
        return errorType;
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
