package com.kite.trading.algo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PositionSizer {

    public int size(BigDecimal availableCapital, BigDecimal price, BigDecimal maxCapitalPerTradePct) {
        if (availableCapital.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal maxNotional = availableCapital.multiply(maxCapitalPerTradePct)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return maxNotional.divide(price, 0, RoundingMode.DOWN).intValue();
    }
}
