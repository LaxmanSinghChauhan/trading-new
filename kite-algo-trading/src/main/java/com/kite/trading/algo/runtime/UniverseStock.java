package com.kite.trading.algo.runtime;

import java.math.BigDecimal;

public record UniverseStock(
        String symbol,
        long instrumentToken,
        String exchange,
        String instrumentType,
        BigDecimal lastPrice,
        String source
) {
}
