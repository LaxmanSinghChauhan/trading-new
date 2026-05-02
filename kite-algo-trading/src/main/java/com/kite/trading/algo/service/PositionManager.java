package com.kite.trading.algo.service;

import com.kite.trading.algo.config.MarketProperties;
import com.kite.trading.algo.domain.BrokerMode;
import com.kite.trading.algo.domain.TradeLog;
import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.PositionSnapshot;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.runtime.TradeIntent;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionManager {

    private final SystemConfigService systemConfigService;
    private final TradeLogService tradeLogService;
    private final BrokerGatewayResolver brokerGatewayResolver;
    private final DailyCapitalTracker dailyCapitalTracker;
    private final TickDataStore tickDataStore;
    private final MarketHealthMonitor marketHealthMonitor;
    private final MarketProperties marketProperties;
    private final TelegramAlertService telegramAlertService;

    private final Map<String, ManagedPosition> openPositions = new ConcurrentHashMap<>();

    public synchronized void openPosition(TradeIntent intent, BrokerOrderResult result, BrokerMode brokerMode) {
        TradeLog tradeLog = tradeLogService.openTrade(intent, result, brokerMode);
        ManagedPosition position = ManagedPosition.from(intent, result, tradeLog.getId(), systemConfigService);
        openPositions.put(intent.symbol(), position);
    }

    public synchronized void onTick(Tick tick) {
        ManagedPosition position = openPositions.get(tick.symbol());
        if (position == null) {
            return;
        }

        if (marketHealthMonitor.isBreakerActive(systemConfigService.marketFallThresholdPct())) {
            exitPosition(position.symbol(), "MARKET_FALL");
            return;
        }
        if (dailyCapitalTracker.isDailyLossBreakerTripped(systemConfigService.maxDailyCapital(), systemConfigService.maxDailyLossPct())) {
            exitPosition(position.symbol(), "DAILY_LOSS_LIMIT");
            return;
        }

        position.updateHigh(tick.lastPrice(), systemConfigService.trailingDistancePct(), systemConfigService.trailingActivationPct());

        if (tick.lastPrice().compareTo(position.stopLossPrice()) <= 0) {
            exitRemaining(position, "STOP_LOSS");
            return;
        }

        if (!position.target1Hit() && position.target1Quantity() > 0 && tick.lastPrice().compareTo(position.target1Price()) >= 0) {
            exitPartial(position, position.target1Quantity(), "TARGET_1");
        }

        ManagedPosition refreshedPosition = openPositions.get(tick.symbol());
        if (refreshedPosition == null) {
            return;
        }

        if (!refreshedPosition.target2Hit() && refreshedPosition.target2Quantity() > 0 && tick.lastPrice().compareTo(refreshedPosition.target2Price()) >= 0) {
            exitPartial(refreshedPosition, refreshedPosition.target2Quantity(), "TARGET_2");
        }

        refreshedPosition = openPositions.get(tick.symbol());
        if (refreshedPosition != null && refreshedPosition.trailingActive()
                && refreshedPosition.trailStopPrice() != null
                && tick.lastPrice().compareTo(refreshedPosition.trailStopPrice()) <= 0) {
            exitRemaining(refreshedPosition, "TRAILING_STOP");
        }
    }

    public synchronized void exitPosition(String symbol, String reason) {
        ManagedPosition position = openPositions.get(symbol);
        if (position == null) {
            return;
        }
        exitRemaining(position, reason);
    }

    public synchronized void exitAll(String reason) {
        new ArrayList<>(openPositions.keySet()).forEach(symbol -> exitPosition(symbol, reason));
    }

    public boolean hasOpenPosition(String symbol) {
        return openPositions.containsKey(symbol);
    }

    public int openPositionCount() {
        return openPositions.size();
    }

    public List<PositionSnapshot> snapshots() {
        return openPositions.values().stream()
                .map(position -> {
                    BigDecimal lastPrice = tickDataStore.getLatestTick(position.instrumentToken())
                            .map(Tick::lastPrice)
                            .orElse(position.entryPrice());
                    BigDecimal unrealized = lastPrice.subtract(position.entryPrice())
                            .multiply(BigDecimal.valueOf(position.remainingQuantity()))
                            .setScale(4, RoundingMode.HALF_UP);
                    return new PositionSnapshot(
                            position.symbol(),
                            position.instrumentToken(),
                            position.entryPrice(),
                            lastPrice,
                            position.totalQuantity(),
                            position.remainingQuantity(),
                            unrealized,
                            position.stopLossPrice(),
                            position.target1Price(),
                            position.target2Price(),
                            position.trailStopPrice(),
                            position.trailingActive(),
                            "OPEN"
                    );
                })
                .toList();
    }

    @Scheduled(cron = "${market.hard-close-cron:0 15 15 * * MON-FRI}", zone = "Asia/Kolkata")
    public void hardClose() {
        exitAll("EOD_CLOSE");
    }

    @PreDestroy
    void shutdown() {
        if (!openPositions.isEmpty()) {
            exitAll("APP_SHUTDOWN");
        }
    }

    private void exitPartial(ManagedPosition position, int requestedQuantity, String reason) {
        int quantity = Math.min(requestedQuantity, position.remainingQuantity());
        if (quantity <= 0) {
            return;
        }
        BrokerOrderResult sellResult = executeSellWithRetry(position, quantity, reason);
        if (sellResult == null) {
            return;
        }
        position.applyExit(reason, quantity);
        TradeLog tradeLog = tradeLogService.recordExit(position.tradeId(), sellResult, reason);
        if (position.remainingQuantity() <= 0) {
            closePosition(position, tradeLog);
        }
    }

    private void exitRemaining(ManagedPosition position, String reason) {
        exitPartial(position, position.remainingQuantity(), reason);
    }

    private BrokerOrderResult executeSellWithRetry(ManagedPosition position, int quantity, String reason) {
        BrokerGateway gateway = brokerGatewayResolver.current();
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return gateway.placeSell(position.symbol(), position.instrumentToken(), quantity, reason);
            } catch (Exception exception) {
                if (attempt % 3 == 0) {
                    telegramAlertService.send("SELL retry " + attempt + " failed for " + position.symbol() + " due to " + exception.getMessage());
                }
                sleep(attempt <= 10 ? 1000L : 500L);
            }
        }
    }

    private void closePosition(ManagedPosition position, TradeLog tradeLog) {
        openPositions.remove(position.symbol());
        dailyCapitalTracker.releaseCapital(position.reservedCapital());
        dailyCapitalTracker.recordRealizedPnl(tradeLog.getNetPnl());
        log.info("Closed position {} with net pnl {}", position.symbol(), tradeLog.getNetPnl());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class ManagedPosition {

        private final Long tradeId;
        private final String symbol;
        private final long instrumentToken;
        private final BigDecimal entryPrice;
        private final int totalQuantity;
        private final BigDecimal reservedCapital;
        private final BigDecimal stopLossPrice;
        private final BigDecimal target1Price;
        private final BigDecimal target2Price;
        private final BigDecimal trailingActivationPrice;
        private final int target1Quantity;
        private final int target2Quantity;
        private int remainingQuantity;
        private boolean target1Hit;
        private boolean target2Hit;
        private boolean trailingActive;
        private BigDecimal highestPrice;
        private BigDecimal trailStopPrice;

        private ManagedPosition(Long tradeId,
                                String symbol,
                                long instrumentToken,
                                BigDecimal entryPrice,
                                int totalQuantity,
                                int remainingQuantity,
                                BigDecimal reservedCapital,
                                BigDecimal stopLossPrice,
                                BigDecimal target1Price,
                                BigDecimal target2Price,
                                BigDecimal trailingActivationPrice,
                                int target1Quantity,
                                int target2Quantity) {
            this.tradeId = tradeId;
            this.symbol = symbol;
            this.instrumentToken = instrumentToken;
            this.entryPrice = entryPrice;
            this.totalQuantity = totalQuantity;
            this.remainingQuantity = remainingQuantity;
            this.reservedCapital = reservedCapital;
            this.stopLossPrice = stopLossPrice;
            this.target1Price = target1Price;
            this.target2Price = target2Price;
            this.trailingActivationPrice = trailingActivationPrice;
            this.target1Quantity = target1Quantity;
            this.target2Quantity = target2Quantity;
            this.highestPrice = entryPrice;
        }

        static ManagedPosition from(TradeIntent intent, BrokerOrderResult result, Long tradeId, SystemConfigService configService) {
            int totalQty = result.quantity();
            int target1Qty = totalQty >= 3 ? Math.max(1, (int) Math.floor(totalQty * 0.4d)) : 0;
            int remainingAfterTarget1 = Math.max(totalQty - target1Qty, 0);
            int target2Qty = remainingAfterTarget1 >= 2 ? Math.max(1, (int) Math.floor(totalQty * 0.4d)) : 0;
            if (target1Qty + target2Qty >= totalQty) {
                target2Qty = Math.max(totalQty - target1Qty - 1, 0);
            }
            BigDecimal entryPrice = result.filledPrice();
            return new ManagedPosition(
                    tradeId,
                    intent.symbol(),
                    intent.instrumentToken(),
                    entryPrice,
                    totalQty,
                    totalQty,
                    intent.reservedCapital(),
                    adjust(entryPrice, configService.stopLossPct().negate()),
                    adjust(entryPrice, configService.target1Pct()),
                    adjust(entryPrice, configService.target2Pct()),
                    adjust(entryPrice, configService.trailingActivationPct()),
                    target1Qty,
                    target2Qty
            );
        }

        void updateHigh(BigDecimal currentPrice, BigDecimal trailingDistancePct, BigDecimal trailingActivationPct) {
            if (currentPrice.compareTo(highestPrice) > 0) {
                highestPrice = currentPrice;
            }
            if (currentPrice.compareTo(trailingActivationPrice) >= 0) {
                trailingActive = true;
            }
            if (trailingActive) {
                trailStopPrice = highestPrice.multiply(BigDecimal.ONE.subtract(
                                trailingDistancePct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
                        .setScale(4, RoundingMode.HALF_UP);
            }
        }

        void applyExit(String reason, int quantity) {
            remainingQuantity = Math.max(remainingQuantity - quantity, 0);
            if ("TARGET_1".equals(reason)) {
                target1Hit = true;
            }
            if ("TARGET_2".equals(reason)) {
                target2Hit = true;
            }
        }

        private static BigDecimal adjust(BigDecimal base, BigDecimal pct) {
            return base.multiply(BigDecimal.ONE.add(
                            pct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
                    .setScale(4, RoundingMode.HALF_UP);
        }

        Long tradeId() {
            return tradeId;
        }

        String symbol() {
            return symbol;
        }

        long instrumentToken() {
            return instrumentToken;
        }

        BigDecimal entryPrice() {
            return entryPrice;
        }

        int totalQuantity() {
            return totalQuantity;
        }

        int remainingQuantity() {
            return remainingQuantity;
        }

        BigDecimal reservedCapital() {
            return reservedCapital;
        }

        BigDecimal stopLossPrice() {
            return stopLossPrice;
        }

        BigDecimal target1Price() {
            return target1Price;
        }

        BigDecimal target2Price() {
            return target2Price;
        }

        BigDecimal trailingActivationPrice() {
            return trailingActivationPrice;
        }

        int target1Quantity() {
            return target1Quantity;
        }

        int target2Quantity() {
            return target2Quantity;
        }

        boolean target1Hit() {
            return target1Hit;
        }

        boolean target2Hit() {
            return target2Hit;
        }

        boolean trailingActive() {
            return trailingActive;
        }

        BigDecimal trailStopPrice() {
            return trailStopPrice;
        }
    }
}
