package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tick.queue")
@Data
public class TickQueueProperties {

    private int capacity = 50_000;
}
