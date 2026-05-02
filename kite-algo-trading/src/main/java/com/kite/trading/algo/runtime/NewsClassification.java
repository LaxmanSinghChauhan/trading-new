package com.kite.trading.algo.runtime;

import java.math.BigDecimal;

public record NewsClassification(
        String sentiment,
        BigDecimal confidence
) {
}
