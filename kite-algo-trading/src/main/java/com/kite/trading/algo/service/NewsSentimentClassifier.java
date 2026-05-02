package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.NewsClassification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class NewsSentimentClassifier {

    private static final Set<String> BULLISH = Set.of("surge", "beats", "growth", "record", "upgrade", "rally", "profit");
    private static final Set<String> BEARISH = Set.of("fall", "drops", "miss", "downgrade", "loss", "fraud", "probe", "weak");

    public NewsClassification classify(String headline) {
        String normalized = headline.toLowerCase();
        for (String word : BEARISH) {
            if (normalized.contains(word)) {
                return new NewsClassification("BEARISH", new BigDecimal("0.75"));
            }
        }
        for (String word : BULLISH) {
            if (normalized.contains(word)) {
                return new NewsClassification("BULLISH", new BigDecimal("0.70"));
            }
        }
        return new NewsClassification("NEUTRAL", new BigDecimal("0.50"));
    }
}
