package com.kite.trading.algo.runtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Tick(
        long instrumentToken,
        String symbol,
        BigDecimal lastPrice,
        long volume,
        LocalDateTime tickTime
) {
}
