package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "market")
@Data
public class MarketProperties {

    private LocalTime hoursStart = LocalTime.of(9, 15);
    private LocalTime signalCutoff = LocalTime.of(15, 20);
    private LocalTime hardClose = LocalTime.of(15, 15);
    private String hardCloseCron = "0 15 15 * * MON-FRI";
    private String capitalResetCron = "0 0 18 * * MON-FRI";
}
