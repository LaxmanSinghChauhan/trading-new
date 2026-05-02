package com.kite.trading.algo.runtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeIntent(
        String symbol,
        long instrumentToken,
        BigDecimal triggerPrice,
        SignalStrength signalStrength,
        int quantity,
        BigDecimal estimatedCharges,
        BigDecimal reservedCapital,
        LocalDateTime createdAt
) {
}
