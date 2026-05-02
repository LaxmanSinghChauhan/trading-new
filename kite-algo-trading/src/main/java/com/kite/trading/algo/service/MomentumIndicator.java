package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.SignalStrength;
import com.kite.trading.algo.runtime.Tick;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MomentumIndicator {

    public BigDecimal priceAccelerationPct(List<Tick> ticks, int windowSeconds) {
        if (ticks.size() < 2) {
            return BigDecimal.ZERO;
        }

        Tick latest = ticks.get(ticks.size() - 1);
        Tick baseline = latest;
        LocalDateTime lowerBound = latest.tickTime().minusSeconds(windowSeconds);

        for (Tick tick : ticks) {
            if (!tick.tickTime().isBefore(lowerBound)) {
                baseline = tick;
                break;
            }
        }

        if (baseline.lastPrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return latest.lastPrice().subtract(baseline.lastPrice())
                .multiply(BigDecimal.valueOf(100))
                .divide(baseline.lastPrice(), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal volumeMultiplier(List<Tick> ticks) {
        if (ticks.size() < 2) {
            return BigDecimal.ZERO;
        }
        long latestVolume = ticks.get(ticks.size() - 1).volume();
        long total = 0L;
        for (int index = 0; index < ticks.size() - 1; index++) {
            total += ticks.get(index).volume();
        }
        BigDecimal average = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(ticks.size() - 1), 4, RoundingMode.HALF_UP);
        if (average.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(latestVolume).divide(average, 4, RoundingMode.HALF_UP);
    }

    public boolean hasConsecutiveHigherTicks(List<Tick> ticks, int confirmationTicks) {
        if (ticks.size() < confirmationTicks) {
            return false;
        }
        for (int index = ticks.size() - confirmationTicks + 1; index < ticks.size(); index++) {
            if (ticks.get(index).lastPrice().compareTo(ticks.get(index - 1).lastPrice()) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean microTrendUp(List<Tick> ticks) {
        if (ticks.size() < 5) {
            return false;
        }
        int start = ticks.size() - 5;
        int advancing = 0;
        for (int index = start + 1; index < ticks.size(); index++) {
            if (ticks.get(index).lastPrice().compareTo(ticks.get(index - 1).lastPrice()) > 0) {
                advancing++;
            }
        }
        return advancing >= 4;
    }

    public boolean isNoisySpike(List<Tick> ticks) {
        if (ticks.size() < 3) {
            return false;
        }
        Tick first = ticks.get(ticks.size() - 3);
        Tick second = ticks.get(ticks.size() - 2);
        Tick third = ticks.get(ticks.size() - 1);

        BigDecimal jumpPct = pctChange(first.lastPrice(), second.lastPrice());
        BigDecimal reversePct = pctChange(second.lastPrice(), third.lastPrice());

        return jumpPct.compareTo(new BigDecimal("0.40")) >= 0
                && reversePct.compareTo(new BigDecimal("-0.20")) <= 0;
    }

    public SignalStrength classifyStrength(BigDecimal accelerationPct) {
        if (accelerationPct.compareTo(new BigDecimal("1.0")) >= 0) {
            return SignalStrength.STRONG;
        }
        if (accelerationPct.compareTo(new BigDecimal("0.7")) >= 0) {
            return SignalStrength.MODERATE;
        }
        return SignalStrength.WEAK;
    }

    private BigDecimal pctChange(BigDecimal from, BigDecimal to) {
        if (from.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return to.subtract(from)
                .multiply(BigDecimal.valueOf(100))
                .divide(from, 4, RoundingMode.HALF_UP);
    }
}
