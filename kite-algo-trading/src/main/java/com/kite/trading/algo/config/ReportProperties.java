package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "report")
@Data
public class ReportProperties {

    private LocalTime summaryTime = LocalTime.of(15, 45);
    private String summaryCron = "0 45 15 * * MON-FRI";
}
