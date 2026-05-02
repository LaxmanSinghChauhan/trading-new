package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.Tick;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MarketHealthMonitor {

    public static final long NIFTY_50_TOKEN = 256265L;

    @Getter
    private BigDecimal niftyDayOpen;

    @Getter
    private BigDecimal latestNiftyPrice;

    public void onTick(Tick tick) {
        if (tick.instrumentToken() != NIFTY_50_TOKEN) {
            return;
        }
        if (niftyDayOpen == null) {
            niftyDayOpen = tick.lastPrice();
        }
        latestNiftyPrice = tick.lastPrice();
    }

    public BigDecimal dropPct() {
        if (niftyDayOpen == null || latestNiftyPrice == null || niftyDayOpen.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return niftyDayOpen.subtract(latestNiftyPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(niftyDayOpen, 4, RoundingMode.HALF_UP);
    }

    public boolean isBreakerActive(BigDecimal thresholdPct) {
        return dropPct().compareTo(thresholdPct) >= 0;
    }
}
