package com.kite.trading.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final int rateLimit;
    private final long rateLimitPeriod;
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimitInterceptor(int rateLimit, long rateLimitPeriod) {
        this.rateLimit = rateLimit;
        this.rateLimitPeriod = rateLimitPeriod;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        long currentTime = System.currentTimeMillis();

        // Reset counter if period has passed
        if (currentTime - info.startTime > rateLimitPeriod) {
            info.reset(currentTime);
        }

        // Check rate limit
        if (info.counter.get() >= rateLimit) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + rateLimit + " requests per minute.\"}");
            return false;
        }

        info.counter.incrementAndGet();
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class RateLimitInfo {
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        void reset(long currentTime) {
            counter.set(0);
            startTime = currentTime;
        }
    }
}