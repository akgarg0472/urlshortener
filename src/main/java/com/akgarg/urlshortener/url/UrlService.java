package com.akgarg.urlshortener.url;

import com.akgarg.urlshortener.request.ShortUrlRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

public interface UrlService {

    String generateShortUrl(HttpServletRequest httpRequest, ShortUrlRequest originalUrl);

    URI getOriginalUrl(HttpServletRequest httpRequest, String shortUrl);

}
