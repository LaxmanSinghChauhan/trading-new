package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramProperties {

    private boolean enabled;
    private String botToken;
    private String chatId;
}
