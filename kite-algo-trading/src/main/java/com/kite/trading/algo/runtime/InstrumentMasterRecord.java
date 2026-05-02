package com.kite.trading.algo.runtime;

import java.math.BigDecimal;

public record InstrumentMasterRecord(
        long instrumentToken,
        String symbol,
        String exchange,
        String instrumentType,
        BigDecimal lastPrice
) {
}
