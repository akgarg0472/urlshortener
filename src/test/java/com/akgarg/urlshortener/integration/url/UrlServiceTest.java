package com.akgarg.urlshortener.integration.url;

import com.akgarg.urlshortener.customalias.v1.CustomAliasService;
import com.akgarg.urlshortener.encoding.EncoderService;
import com.akgarg.urlshortener.exception.UrlShortenerException;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.request.ShortUrlRequest;
import com.akgarg.urlshortener.statistics.StatisticsService;
import com.akgarg.urlshortener.unit.faker.FakerService;
import com.akgarg.urlshortener.url.v1.UrlService;
import com.akgarg.urlshortener.url.v1.db.UrlDatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class UrlServiceTest {

    private static final String DOMAIN = "http://localhost:8080/";
    private static final String ATTRIBUTE_REQUEST_ID = "requestId";
    private static final String HEADER_USER_AGENT = "USER-AGENT";

    @Mock
    private EncoderService encoderService;
    @Mock
    private UrlDatabaseService urlDatabaseService;
    @Mock
    private StatisticsService statisticsService;
    @Mock
    private NumberGeneratorService numberGeneratorService;
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private CustomAliasService customAliasService;

    private AutoCloseable closeable;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        urlService = new UrlService(
                encoderService,
                urlDatabaseService,
                statisticsService,
                numberGeneratorService,
                customAliasService,
                DOMAIN
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        System.out.println();
    }

    @Test
    void generateShortUrl_ShouldReturn_ShortUrl() {
        final var number = 1_00_00_00_00_000L;
        final var shortUrl = "O9Oz9L1";
        final var userId = "4b34ed1400fd06ef21f";
        final var originalUrl = "https://www.google.com";
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var expectedShortUrl = DOMAIN + shortUrl;

        when(numberGeneratorService.generateNextNumber()).thenReturn(number);
        when(encoderService.encode(number)).thenReturn(shortUrl);
        when(urlDatabaseService.saveUrl(ArgumentMatchers.any())).thenReturn(true);
        when(httpRequest.getAttribute("requestId")).thenReturn(System.nanoTime());
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        final var request = new ShortUrlRequest(userId, originalUrl, null, null);
        final var generatedShorUrl = urlService.generateShortUrl(httpRequest, request);

        verify(numberGeneratorService, times(1)).generateNextNumber();
        verify(encoderService, times(1)).encode(number);
        verify(urlDatabaseService, times(1)).saveUrl(ArgumentMatchers.any());
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);

        assertEquals(expectedShortUrl, generatedShorUrl.shortUrl(), "Short url should be same as expected short url");
    }

    @Test
    void generateShortUrl_ShouldThrowUrlShortenerException_WhenNumberGeneratorServiceReturnsZero() {
        final var number = 0L;
        final var requestId = System.nanoTime();
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var userId = "4b34ed1400fd06ef21f";
        final var originalUrl = "https://www.google.com";

        when(numberGeneratorService.generateNextNumber()).thenReturn(number);
        when(httpRequest.getAttribute("requestId")).thenReturn(requestId);
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        final var request = new ShortUrlRequest(userId, originalUrl, null, null);

        assertThrows(
                UrlShortenerException.class,
                () -> urlService.generateShortUrl(httpRequest, request),
                "generateShortUrl method should throw UrlShortenerException when number generator service returns zero"
        );

        verify(numberGeneratorService, times(1)).generateNextNumber();
        verify(encoderService, times(0)).encode(number);
        verify(urlDatabaseService, times(0)).saveUrl(ArgumentMatchers.any());
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);
    }

    @Test
    void generateShortUrl_ShouldThrowUrlShortenerException_WhenNumberGeneratorServiceReturnsNegativeNumber() {
        final var number = 0L;
        final var requestId = System.nanoTime();
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var userId = "4b34ed1400fd06ef21f";
        final var originalUrl = "https://www.google.com";

        when(numberGeneratorService.generateNextNumber()).thenReturn(number);
        when(httpRequest.getAttribute("requestId")).thenReturn(requestId);
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        final var request = new ShortUrlRequest(userId, originalUrl, null, null);

        assertThrows(
                UrlShortenerException.class,
                () -> urlService.generateShortUrl(httpRequest, request),
                "generateShortUrl method should throw UrlShortenerException when number generator service returns negative number"
        );

        verify(numberGeneratorService, times(1)).generateNextNumber();
        verify(encoderService, times(0)).encode(number);
        verify(urlDatabaseService, times(0)).saveUrl(ArgumentMatchers.any());
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);
    }

    @Test
    void generateShortUrl_ShouldThrowUrlShortenerException_WhenDatabaseSaveFailed() {
        final var number = 1_00_00_00_00_000L;
        final var shortUrl = "O9Oz9L1";
        final var requestId = System.nanoTime();
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var userId = "4b34ed1400fd06ef21f";
        final var originalUrl = "https://www.google.com";

        when(numberGeneratorService.generateNextNumber()).thenReturn(number);
        when(encoderService.encode(number)).thenReturn(shortUrl);
        when(urlDatabaseService.saveUrl(ArgumentMatchers.any())).thenReturn(false);
        when(httpRequest.getAttribute("requestId")).thenReturn(requestId);
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        final var request = new ShortUrlRequest(userId, originalUrl, null, null);

        assertThrows(
                UrlShortenerException.class,
                () -> urlService.generateShortUrl(httpRequest, request),
                "generateShortUrl method should throw UrlShortenerException when database save failed"
        );

        verify(numberGeneratorService, times(1)).generateNextNumber();
        verify(encoderService, times(1)).encode(number);
        verify(urlDatabaseService, times(1)).saveUrl(ArgumentMatchers.any());
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);
    }

    @Test
    void getShortUrl_ShouldReturn_OriginalUrl() {
        final var urlMetadata = FakerService.fakeUrlMetadata();
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var shortUrl = urlMetadata.getShortUrl();
        final var expectedOriginalUrl = URI.create(urlMetadata.getOriginalUrl());

        when(urlDatabaseService.getUrlByShortUrl(shortUrl)).thenReturn(Optional.of(urlMetadata));
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        final var originalUrlFromUrlService = urlService.getOriginalUrl(httpRequest, shortUrl);

        assertEquals(
                expectedOriginalUrl,
                originalUrlFromUrlService,
                "Original url should be same as expected original url"
        );

        verify(urlDatabaseService, times(1)).getUrlByShortUrl(shortUrl);
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);
    }

    @Test
    void getShortUrl_ShouldThrowUrlShortenerException_WhenUrlMetadataNotFound() {
        final var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64";
        final var shortUrl = "O9Oz9L1";

        when(urlDatabaseService.getUrlByShortUrl(shortUrl)).thenReturn(Optional.empty());
        when(httpRequest.getHeader("USER-AGENT")).thenReturn(userAgent);

        assertThrowsExactly(
                UrlShortenerException.class,
                () -> urlService.getOriginalUrl(httpRequest, shortUrl),
                "getOriginalUrl method should throw UrlShortenerException when url metadata not found"
        );

        verify(urlDatabaseService, times(1)).getUrlByShortUrl(shortUrl);
        verify(httpRequest, times(2)).getAttribute(ATTRIBUTE_REQUEST_ID);
        verify(httpRequest, times(1)).getHeader(HEADER_USER_AGENT);
    }

}
