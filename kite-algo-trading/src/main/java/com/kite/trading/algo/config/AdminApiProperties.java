package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin")
@Data
public class AdminApiProperties {

    private String apiKey = "change-me";
}
