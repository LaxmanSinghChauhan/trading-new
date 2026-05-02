package com.kite.trading.algo.runtime;

import java.math.BigDecimal;

public record PositionSnapshot(
        String symbol,
        long instrumentToken,
        BigDecimal entryPrice,
        BigDecimal lastPrice,
        int totalQuantity,
        int remainingQuantity,
        BigDecimal unrealizedPnl,
        BigDecimal stopLossPrice,
        BigDecimal target1Price,
        BigDecimal target2Price,
        BigDecimal trailStopPrice,
        boolean trailingActive,
        String status
) {
}
