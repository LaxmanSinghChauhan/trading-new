package com.kite.trading.algo.runtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Signal(
        String symbol,
        long instrumentToken,
        BigDecimal lastPrice,
        BigDecimal priceChangePct,
        BigDecimal volumeMultiplier,
        SignalStrength strength,
        LocalDateTime detectedAt
) {
}
