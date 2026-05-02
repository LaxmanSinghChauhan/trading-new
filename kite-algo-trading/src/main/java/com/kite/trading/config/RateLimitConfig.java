package com.kite.trading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Value("${ratelimit.limit:35}")
    private int rateLimit;

    @Value("${ratelimit.period:60000}")
    private long rateLimitPeriod;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimit, rateLimitPeriod));
    }
}