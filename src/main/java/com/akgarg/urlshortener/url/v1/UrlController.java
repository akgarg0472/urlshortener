package com.akgarg.urlshortener.url.v1;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
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

import java.time.LocalDateTime;
import java.util.Map;

import static com.akgarg.urlshortener.utils.UrlShortenerUtil.checkValidationResultAndThrowExceptionOnFailedValidation;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "API for shortening and expanding URLs")
public class UrlController {

    private final UrlService urlService;

    @Operation(summary = "Ping the server", description = "This endpoint is used to check if the service is running.")
    @GetMapping("/api/v1/ping")
    public Map<String, String> heartbeat(
            @Parameter(description = "HTTP Request Object", required = true) final HttpServletRequest httpRequest
    ) {
        return Map.of(
                "message", "PONG!!",
                "time", LocalDateTime.now().toString(),
                "clientIp", httpRequest.getRemoteAddr()
        );
    }

    @Operation(summary = "Generate a Short URL", description = "This endpoint accepts a long URL and generates a shortened URL.")
    @PostMapping("/api/v1/urlshortener")
    public ResponseEntity<GenerateUrlResponse> generateShortUrl(
            @Parameter(description = "HTTP Request Object", required = true) final HttpServletRequest httpRequest,
            @Parameter(description = "Request body containing the URL to shorten", required = true)
            @Valid @RequestBody final ShortUrlRequest request,
            final BindingResult validationResult
    ) {
        checkValidationResultAndThrowExceptionOnFailedValidation(validationResult);
        GenerateUrlResponse response = urlService.generateShortUrl(httpRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Redirect to the original URL", description = "This endpoint accepts a shortened URL and redirects to the original URL.")
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> getAndRedirectToOriginalUrl(
            @Parameter(description = "Short URL code to redirect to the original URL", required = true)
            @PathVariable final String shortUrl,
            @Parameter(description = "HTTP Request Object", required = true) final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(urlService.getOriginalUrl(httpRequest, shortUrl))
                .build();
    }
}
