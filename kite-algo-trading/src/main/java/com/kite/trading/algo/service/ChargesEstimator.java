package com.kite.trading.algo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ChargesEstimator {

    private static final BigDecimal RATE = new BigDecimal("0.0005");

    public BigDecimal estimate(BigDecimal notional) {
        return notional.multiply(RATE).setScale(4, RoundingMode.HALF_UP);
    }
}
