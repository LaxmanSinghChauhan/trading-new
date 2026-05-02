package com.kite.trading.algo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "news")
@Data
public class NewsProperties {

    private boolean enabled;
    private long pollIntervalMs = 120_000L;
    private int topSymbolLimit = 20;
    private String nseUrl;
    private String googleRssTemplate = "https://news.google.com/rss/search?q=%s+stock";
}
