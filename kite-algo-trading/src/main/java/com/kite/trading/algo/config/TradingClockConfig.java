package com.kite.trading.algo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TradingClockConfig {

    @Bean
    public Clock tradingClock() {
        return Clock.system(ZoneId.of("Asia/Kolkata"));
    }
}
