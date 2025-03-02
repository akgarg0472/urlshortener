package com.akgarg.urlshortener.v1.api;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.checkValidationResultAndThrowExceptionOnFailedValidation;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "API for shortening and expanding URLs")
public class UrlController {

    private final UrlService urlService;

    @Operation(summary = "Generate a Short URL", description = "This endpoint accepts a long URL and generates a shortened URL.")
    @PostMapping("/api/v1/urlshortener")
    public ResponseEntity<Object> generateShortUrl(
            @Parameter(description = "HTTP Request Object", required = true) final HttpServletRequest httpRequest,

            @Parameter(description = "Request body containing the URL to shorten", required = true)
            @Valid @RequestBody final ShortUrlRequest request,
            final BindingResult validationResult
    ) {
        checkValidationResultAndThrowExceptionOnFailedValidation(validationResult);
        final var response = urlService.generateShortUrl(httpRequest, request);
        return ResponseEntity.status(response.statusCode()).body(response.data());
    }

    @Operation(summary = "Redirect to the original URL", description = "This endpoint accepts a shortened URL and redirects to the original URL.")
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> getAndRedirectToOriginalUrl(
            @Parameter(description = "Short URL code to redirect to the original URL", required = true)
            @PathVariable final String shortUrl,
            @Parameter(description = "HTTP Request Object", required = true) final HttpServletRequest httpRequest
    ) {
        final var response = urlService.getOriginalUrl(httpRequest, shortUrl);
        if (response.statusCode() == HttpStatus.OK.value()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location((URI) response.data())
                    .build();
        }
        return ResponseEntity.status(response.statusCode()).body(response.data());
    }

}
