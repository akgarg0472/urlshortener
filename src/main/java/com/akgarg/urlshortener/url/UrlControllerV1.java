package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import static com.akgarg.urlshortener.utils.UrlShortenerUtility.checkValidationResultAndThrowExceptionOnFailedValidation;

@RestController
public class UrlControllerV1 {

    private final UrlService urlService;

    public UrlControllerV1(final UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/api/v1/ping")
    public Map<String, String> heartbeat(final HttpServletRequest httpRequest) {
        return Map.of("message", "PONG!!",
                      "time", LocalDateTime.now().toString(),
                      "clientIp", httpRequest.getRemoteAddr()
        );
    }

    @PostMapping("/api/v1/urlshortener")
    public ResponseEntity<GenerateUrlResponse> generateShortUrl(
            final HttpServletRequest httpRequest,
            @Valid @RequestBody final ShortUrlRequest request,
            final BindingResult validationResult
    ) {
        checkValidationResultAndThrowExceptionOnFailedValidation(validationResult);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.generateShortUrl(httpRequest, request));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> getAndRedirectToOriginalUrl(
            final HttpServletRequest httpRequest,
            @PathVariable final String shortUrl
    ) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(urlService.getOriginalUrl(httpRequest, shortUrl))
                .build();
    }

}
