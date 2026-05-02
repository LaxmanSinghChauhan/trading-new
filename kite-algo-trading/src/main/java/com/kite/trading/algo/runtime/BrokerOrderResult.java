package com.kite.trading.algo.runtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BrokerOrderResult(
        String orderId,
        BigDecimal filledPrice,
        int quantity,
        LocalDateTime executedAt
) {
}
