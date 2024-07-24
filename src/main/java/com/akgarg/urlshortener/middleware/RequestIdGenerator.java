package com.akgarg.urlshortener.middleware;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdGenerator extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nonnull final FilterChain filterChain
    ) throws ServletException, IOException {
        request.setAttribute("requestId", generateRequestId());
        filterChain.doFilter(request, response);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
