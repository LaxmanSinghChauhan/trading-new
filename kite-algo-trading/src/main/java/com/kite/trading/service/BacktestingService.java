package com.kite.trading.service;

import com.kite.trading.model.MarketData;
import com.kite.trading.strategy.SuddenMovementStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestingService {

    private final MarketDataService marketDataService;
    private final SuddenMovementStrategy suddenMovementStrategy;

    public BacktestResult runBacktest(BacktestRequest request) {
        try {
            log.info("Starting backtest for symbol: {} from {} to {}",
                    request.getSymbol(), request.getStartDate(), request.getEndDate());

            List<MarketData> historicalData = marketDataService.getHistoricalDataFromDB(
                    request.getSymbol(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            if (historicalData.isEmpty()) {
                throw new IllegalArgumentException("No historical data found for the given period");
            }

            BacktestResult result = new BacktestResult();
            result.setSymbol(request.getSymbol());
            result.setStartDate(request.getStartDate());
            result.setEndDate(request.getEndDate());
            result.setStrategyName(request.getStrategyName());

            List<BacktestTrade> trades = new ArrayList<>();
            BigDecimal initialCapital = request.getInitialCapital();
            BigDecimal currentCapital = initialCapital;
            int position = 0;
            BigDecimal entryPrice = BigDecimal.ZERO;

            for (int i = 0; i < historicalData.size(); i++) {
                MarketData data = historicalData.get(i);

                if (i >= 20) {
                    List<MarketData> recentData = historicalData.subList(i - 20, i);
                    List<BigDecimal> prices = recentData.stream()
                            .map(MarketData::getClose)
                            .toList();

                    BigDecimal rsi = suddenMovementStrategy.calculateRSI(prices, 14);
                    SuddenMovementStrategy.MACDResult macd = suddenMovementStrategy.calculateMACD(prices, 12, 26, 9);

                    TradingSignal signal = generateTradingSignal(data, rsi, macd);

                    if (signal == TradingSignal.BUY && position == 0) {
                        position = 1;
                        entryPrice = data.getClose();
                        log.debug("BUY signal at {} on {}", entryPrice, data.getTimestamp());
                    } else if (signal == TradingSignal.SELL && position == 1) {
                        BigDecimal exitPrice = data.getClose();
                        BigDecimal pnl = exitPrice.subtract(entryPrice);
                        currentCapital = currentCapital.add(pnl);

                        BacktestTrade trade = new BacktestTrade();
                        trade.setEntryTime(historicalData.get(i - 1).getTimestamp());
                        trade.setExitTime(data.getTimestamp());
                        trade.setEntryPrice(entryPrice);
                        trade.setExitPrice(exitPrice);
                        trade.setPnl(pnl);
                        trade.setPnlPercentage(pnl.divide(entryPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
                        trades.add(trade);

                        position = 0;
                        log.debug("SELL signal at {} on {}, P&L: {}", exitPrice, data.getTimestamp(), pnl);
                    }
                }
            }

            result.setTrades(trades);
            result.setInitialCapital(initialCapital);
            result.setFinalCapital(currentCapital);
            result.setTotalReturn(calculateTotalReturn(initialCapital, currentCapital));
            result.setTotalTrades(trades.size());
            result.setWinningTrades((int) trades.stream().filter(t -> t.getPnl().compareTo(BigDecimal.ZERO) > 0).count());
            result.setLosingTrades(trades.size() - result.getWinningTrades());
            result.setWinRate(calculateWinRate(trades));
            result.setMaxDrawdown(calculateMaxDrawdown(trades, initialCapital));
            result.setSharpeRatio(calculateSharpeRatio(trades));
            result.setAverageTradeDuration(calculateAverageTradeDuration(trades));

            log.info("Backtest completed. Total return: {}, Total trades: {}",
                    result.getTotalReturn(), result.getTotalTrades());

            return result;
        } catch (Exception e) {
            log.error("Error running backtest", e);
            throw new RuntimeException("Backtest failed", e);
        }
    }

    private TradingSignal generateTradingSignal(MarketData data, BigDecimal rsi, SuddenMovementStrategy.MACDResult macd) {
        if (rsi.compareTo(BigDecimal.valueOf(30)) < 0 && macd.getHistogram().compareTo(BigDecimal.ZERO) > 0) {
            return TradingSignal.BUY;
        } else if (rsi.compareTo(BigDecimal.valueOf(70)) > 0 && macd.getHistogram().compareTo(BigDecimal.ZERO) < 0) {
            return TradingSignal.SELL;
        }
        return TradingSignal.HOLD;
    }

    private BigDecimal calculateTotalReturn(BigDecimal initialCapital, BigDecimal finalCapital) {
        return finalCapital.subtract(initialCapital)
                .divide(initialCapital, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateWinRate(List<BacktestTrade> trades) {
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long winningTrades = trades.stream().filter(t -> t.getPnl().compareTo(BigDecimal.ZERO) > 0).count();
        return BigDecimal.valueOf(winningTrades)
                .divide(BigDecimal.valueOf(trades.size()), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateMaxDrawdown(List<BacktestTrade> trades, BigDecimal initialCapital) {
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = initialCapital;
        BigDecimal currentCapital = initialCapital;

        for (BacktestTrade trade : trades) {
            currentCapital = currentCapital.add(trade.getPnl());
            if (currentCapital.compareTo(peak) > 0) {
                peak = currentCapital;
            }
            BigDecimal drawdown = peak.subtract(currentCapital)
                    .divide(peak, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown;
    }

    private BigDecimal calculateSharpeRatio(List<BacktestTrade> trades) {
        if (trades.isEmpty() || trades.size() < 2) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> returns = trades.stream()
                .map(BacktestTrade::getPnlPercentage)
                .toList();

        BigDecimal mean = calculateMean(returns);
        BigDecimal stdDev = calculateStandardDeviation(returns, mean);

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return mean.divide(stdDev, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDeviation(List<BigDecimal> values, BigDecimal mean) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = values.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);

        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    private long calculateAverageTradeDuration(List<BacktestTrade> trades) {
        if (trades.isEmpty()) {
            return 0;
        }

        long totalDuration = trades.stream()
                .mapToLong(t -> java.time.Duration.between(t.getEntryTime(), t.getExitTime()).toMinutes())
                .sum();

        return totalDuration / trades.size();
    }

    public static class BacktestRequest {
        private String symbol;
        private String strategyName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal initialCapital = new BigDecimal("100000");

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getStrategyName() { return strategyName; }
        public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public BigDecimal getInitialCapital() { return initialCapital; }
        public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
    }

    public static class BacktestResult {
        private String symbol;
        private String strategyName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal initialCapital;
        private BigDecimal finalCapital;
        private BigDecimal totalReturn;
        private int totalTrades;
        private int winningTrades;
        private int losingTrades;
        private BigDecimal winRate;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private long averageTradeDuration;
        private List<BacktestTrade> trades;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getStrategyName() { return strategyName; }
        public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public BigDecimal getInitialCapital() { return initialCapital; }
        public void setInitialCapital(BigDecimal initialCapital) { this.initialCapital = initialCapital; }
        public BigDecimal getFinalCapital() { return finalCapital; }
        public void setFinalCapital(BigDecimal finalCapital) { this.finalCapital = finalCapital; }
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        public int getTotalTrades() { return totalTrades; }
        public void setTotalTrades(int totalTrades) { this.totalTrades = totalTrades; }
        public int getWinningTrades() { return winningTrades; }
        public void setWinningTrades(int winningTrades) { this.winningTrades = winningTrades; }
        public int getLosingTrades() { return losingTrades; }
        public void setLosingTrades(int losingTrades) { this.losingTrades = losingTrades; }
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
        public long getAverageTradeDuration() { return averageTradeDuration; }
        public void setAverageTradeDuration(long averageTradeDuration) { this.averageTradeDuration = averageTradeDuration; }
        public List<BacktestTrade> getTrades() { return trades; }
        public void setTrades(List<BacktestTrade> trades) { this.trades = trades; }
    }

    public static class BacktestTrade {
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private BigDecimal entryPrice;
        private BigDecimal exitPrice;
        private BigDecimal pnl;
        private BigDecimal pnlPercentage;

        public LocalDateTime getEntryTime() { return entryTime; }
        public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
        public LocalDateTime getExitTime() { return exitTime; }
        public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
        public BigDecimal getEntryPrice() { return entryPrice; }
        public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
        public BigDecimal getExitPrice() { return exitPrice; }
        public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }
        public BigDecimal getPnl() { return pnl; }
        public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
        public BigDecimal getPnlPercentage() { return pnlPercentage; }
        public void setPnlPercentage(BigDecimal pnlPercentage) { this.pnlPercentage = pnlPercentage; }
    }

    private enum TradingSignal {
        BUY, SELL, HOLD
    }
}
