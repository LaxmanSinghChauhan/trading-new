package com.kite.trading.algo.service;

import com.kite.trading.algo.config.MarketProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyCapitalTracker {

    private final MarketProperties marketProperties;

    @Getter
    private final AtomicReference<BigDecimal> reservedCapital = new AtomicReference<>(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));

    @Getter
    private final AtomicReference<BigDecimal> realizedPnl = new AtomicReference<>(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));

    @PostConstruct
    void init() {
        reset();
    }

    public void reserveCapital(BigDecimal amount) {
        reservedCapital.updateAndGet(current -> current.add(amount));
    }

    public void releaseCapital(BigDecimal amount) {
        reservedCapital.updateAndGet(current -> current.subtract(amount).max(BigDecimal.ZERO));
    }

    public void recordRealizedPnl(BigDecimal pnl) {
        realizedPnl.updateAndGet(current -> current.add(pnl));
    }

    public BigDecimal availableCapital(BigDecimal maxDailyCapital) {
        return maxDailyCapital.subtract(reservedCapital.get()).max(BigDecimal.ZERO);
    }

    public boolean isDailyLossBreakerTripped(BigDecimal maxDailyCapital, BigDecimal maxDailyLossPct) {
        BigDecimal lossLimit = maxDailyCapital.multiply(maxDailyLossPct)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return realizedPnl.get().compareTo(lossLimit.negate()) <= 0;
    }

    @Scheduled(cron = "${market.capital-reset-cron:0 0 18 * * MON-FRI}", zone = "Asia/Kolkata")
    public void reset() {
        reservedCapital.set(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        realizedPnl.set(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        log.info("Daily capital tracker reset");
    }
}
