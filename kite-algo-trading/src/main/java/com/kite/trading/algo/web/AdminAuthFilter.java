package com.kite.trading.algo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite.trading.algo.config.AdminApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    private static final String ADMIN_PREFIX = "/api/admin/";

    private final AdminApiProperties adminApiProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(ADMIN_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String actualKey = request.getHeader("X-Admin-Key");
        String expectedKey = adminApiProperties.getApiKey();
        if (expectedKey == null || expectedKey.isBlank() || !expectedKey.equals(actualKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Invalid admin key"));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
