package com.kite.trading.algo.service;

import com.kite.trading.algo.config.MarketProperties;
import com.kite.trading.algo.runtime.Signal;
import com.kite.trading.algo.runtime.TradeIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEngine {

    private final ExecutionQueue executionQueue;
    private final SystemConfigService systemConfigService;
    private final PositionManager positionManager;
    private final DailyCapitalTracker dailyCapitalTracker;
    private final PositionSizer positionSizer;
    private final ChargesEstimator chargesEstimator;
    private final MarketHealthMonitor marketHealthMonitor;
    private final MarketProperties marketProperties;
    private final Clock tradingClock;

    private final AtomicLong approvedSignals = new AtomicLong();
    private final AtomicLong blockedSignals = new AtomicLong();
    private volatile String lastDecision = "NONE";
    private volatile List<String> lastBlockReasons = List.of();

    public void evaluate(Signal signal) {
        List<String> blockReasons = new java.util.ArrayList<>();
        if (!systemConfigService.tradingEnabled()) {
            blockReasons.add("Trading is paused");
        }
        if (!"LONG_ONLY".equalsIgnoreCase(systemConfigService.strategyDirection())) {
            blockReasons.add("Strategy direction is not LONG_ONLY");
        }

        LocalTime now = LocalDateTime.now(tradingClock).toLocalTime();
        if (now.isBefore(marketProperties.getHoursStart()) || now.isAfter(marketProperties.getSignalCutoff())) {
            blockReasons.add("Outside signal window");
        }
        if (dailyCapitalTracker.isDailyLossBreakerTripped(systemConfigService.maxDailyCapital(), systemConfigService.maxDailyLossPct())) {
            blockReasons.add("Daily loss breaker active");
        }
        if (marketHealthMonitor.isBreakerActive(systemConfigService.marketFallThresholdPct())) {
            blockReasons.add("Market fall breaker active");
        }
        if (positionManager.hasOpenPosition(signal.symbol())) {
            blockReasons.add("Symbol already has an open position");
        }
        if (positionManager.openPositionCount() >= systemConfigService.maxOpenPositions()) {
            blockReasons.add("Max open positions reached");
        }

        BigDecimal availableCapital = dailyCapitalTracker.availableCapital(systemConfigService.maxDailyCapital());
        if (availableCapital.compareTo(new BigDecimal("5000")) < 0) {
            blockReasons.add("Available capital below minimum");
        }

        int quantity = positionSizer.size(availableCapital, signal.lastPrice(), systemConfigService.maxCapitalPerTradePct());
        if (quantity < 1) {
            blockReasons.add("Position size resolved below one share");
        }

        if (!blockReasons.isEmpty()) {
            blockedSignals.incrementAndGet();
            lastDecision = "BLOCKED";
            lastBlockReasons = List.copyOf(blockReasons);
            log.info("Signal blocked for {}: {}", signal.symbol(), blockReasons);
            return;
        }

        BigDecimal reservedCapital = signal.lastPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal estimatedCharges = chargesEstimator.estimate(reservedCapital);
        TradeIntent intent = new TradeIntent(
                signal.symbol(),
                signal.instrumentToken(),
                signal.lastPrice(),
                signal.strength(),
                quantity,
                estimatedCharges,
                reservedCapital,
                LocalDateTime.now(tradingClock)
        );
        dailyCapitalTracker.reserveCapital(reservedCapital);
        approvedSignals.incrementAndGet();
        lastDecision = "APPROVED";
        lastBlockReasons = List.of();
        if (!executionQueue.offer(intent)) {
            dailyCapitalTracker.releaseCapital(reservedCapital);
            blockedSignals.incrementAndGet();
            approvedSignals.decrementAndGet();
            lastDecision = "EXECUTION_QUEUE_REJECTED";
            lastBlockReasons = List.of("Execution queue saturated");
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("tradingEnabled", systemConfigService.tradingEnabled());
        status.put("approvedSignals", approvedSignals.get());
        status.put("blockedSignals", blockedSignals.get());
        status.put("openPositions", positionManager.openPositionCount());
        status.put("availableCapital", dailyCapitalTracker.availableCapital(systemConfigService.maxDailyCapital()));
        status.put("reservedCapital", dailyCapitalTracker.getReservedCapital().get());
        status.put("realizedPnl", dailyCapitalTracker.getRealizedPnl().get());
        status.put("marketDropPct", marketHealthMonitor.dropPct());
        status.put("marketBreakerActive", marketHealthMonitor.isBreakerActive(systemConfigService.marketFallThresholdPct()));
        status.put("dailyLossBreakerActive", dailyCapitalTracker.isDailyLossBreakerTripped(systemConfigService.maxDailyCapital(), systemConfigService.maxDailyLossPct()));
        status.put("lastDecision", lastDecision);
        status.put("lastBlockReasons", lastBlockReasons);
        return status;
    }
}
