package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "execution.queue")
@Data
public class ExecutionQueueProperties {

    private int capacity = 1000;
}
