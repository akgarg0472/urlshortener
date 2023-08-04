package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.akgarg.urlshortener.utils.UrlShortenerUtility.checkValidationResultAndThrowExceptionOnFailedValidation;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(final UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/v1/url-shortener")
    public ResponseEntity<String> generateShortUrl(
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
