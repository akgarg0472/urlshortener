package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.response.GenerateUrlResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

public interface UrlService {

    GenerateUrlResponse generateShortUrl(HttpServletRequest httpRequest, ShortUrlRequest originalUrl);

    URI getOriginalUrl(HttpServletRequest httpRequest, String shortUrl);

}
