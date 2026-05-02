package com.kite.trading.strategy;

import com.kite.trading.model.Order;
import com.kite.trading.model.PriceMovement;
import com.kite.trading.model.Strategy;
import com.kite.trading.model.Trade;
import com.kite.trading.repository.PriceMovementRepository;
import com.kite.trading.repository.StrategyRepository;
import com.kite.trading.repository.TradeRepository;
import com.kite.trading.service.MarketDataService;
import com.kite.trading.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class StrategyEngine {

    private final StrategyRepository strategyRepository;
    private final TradeRepository tradeRepository;
    private final PriceMovementRepository priceMovementRepository;
    private final SuddenMovementStrategy suddenMovementStrategy;
    private final MarketDataService marketDataService;
    private final OrderService orderService;

    @Scheduled(fixedRateString = "${strategy.execution.interval:60000}")
    public void executeStrategies() {
        try {
            List<Strategy> activeStrategies = strategyRepository.findByIsActiveTrue();

            for (Strategy strategy : activeStrategies) {
                executeStrategy(strategy);
            }

            log.info("Executed {} active strategies", activeStrategies.size());
        } catch (Exception e) {
            log.error("Error executing strategies", e);
        }
    }

    public void executeStrategy(Strategy strategy) {
        try {
            switch (strategy.getType()) {
                case SUDDEN_MOVEMENT:
                    executeSuddenMovementStrategy(strategy);
                    break;
                default:
                    log.warn("Unsupported strategy type: {}", strategy.getType());
            }

            strategy.setLastExecuted(LocalDateTime.now());
            strategyRepository.save(strategy);
        } catch (Exception e) {
            log.error("Error executing strategy: {}", strategy.getName(), e);
        }
    }

    private void executeSuddenMovementStrategy(Strategy strategy) {
        List<String> watchedSymbols = getWatchedSymbols(strategy);

        for (String symbol : watchedSymbols) {
            try {
                List<BigDecimal> recentPrices = getRecentPrices(symbol, 10);
                List<Long> recentVolumes = getRecentVolumes(symbol, 10);

                if (recentPrices.size() < 2) {
                    continue;
                }

                List<PriceMovement> movements = suddenMovementStrategy.detectSuddenMovements(
                        symbol, recentPrices, recentVolumes);

                for (PriceMovement movement : movements) {
                    processMovement(movement, strategy);
                }
            } catch (Exception e) {
                log.error("Error processing symbol: {}", symbol, e);
            }
        }
    }

    private void processMovement(PriceMovement movement, Strategy strategy) {
        try {
            List<BigDecimal> prices = getRecentPrices(movement.getSymbol(), 20);
            BigDecimal rsi = suddenMovementStrategy.calculateRSI(prices, 14);
            SuddenMovementStrategy.MACDResult macd = suddenMovementStrategy.calculateMACD(prices, 12, 26, 9);

            SuddenMovementStrategy.TradingSignal signal = suddenMovementStrategy.generateTradingSignal(
                    movement, rsi, macd);

            if (signal.getSignalType() == SuddenMovementStrategy.SignalType.BUY ||
                signal.getSignalType() == SuddenMovementStrategy.SignalType.SELL) {

                movement.setRsi(rsi);
                movement.setMacd(macd.getMacd());
                movement.setSignalLine(macd.getSignal());
                movement.setSignalGenerated(true);
                priceMovementRepository.save(movement);

                if (signal.getConfidence().compareTo(BigDecimal.valueOf(0.7)) >= 0) {
                    executeSignal(signal, strategy);
                }
            }
        } catch (Exception e) {
            log.error("Error processing movement", e);
        }
    }

    private void executeSignal(SuddenMovementStrategy.TradingSignal signal, Strategy strategy) {
        try {
            OrderService.OrderRequest orderRequest = new OrderService.OrderRequest();
            orderRequest.setSymbol(signal.getSymbol());
            orderRequest.setExchange("NSE");

            if (signal.getSignalType() == SuddenMovementStrategy.SignalType.BUY) {
                orderRequest.setTransactionType(Order.TransactionType.BUY);
            } else {
                orderRequest.setTransactionType(Order.TransactionType.SELL);
            }

            orderRequest.setOrderType(Order.OrderType.MARKET);
            orderRequest.setQuantity(getStrategyQuantity(strategy));
            orderRequest.setRemarks("Strategy: " + strategy.getName());

            Order order = orderService.placeOrder(orderRequest);

            if (order != null) {
                createTradeFromOrder(order, strategy, signal);
                log.info("Executed {} signal for {} with confidence {}",
                        signal.getSignalType(), signal.getSymbol(), signal.getConfidence());
            }
        } catch (Exception e) {
            log.error("Error executing signal", e);
        }
    }

    private void createTradeFromOrder(Order order, Strategy strategy,
                                      SuddenMovementStrategy.TradingSignal signal) {
        Trade trade = new Trade();
        trade.setTradeId(UUID.randomUUID().toString());
        trade.setSymbol(order.getSymbol());
        trade.setExchange(order.getExchange());
        trade.setStrategyName(strategy.getName());

        if (order.getTransactionType() == Order.TransactionType.BUY) {
            trade.setTradeType(Trade.TradeType.LONG);
        } else {
            trade.setTradeType(Trade.TradeType.SHORT);
        }

        trade.setEntryPrice(order.getAveragePrice());
        trade.setQuantity(order.getQuantity());
        trade.setStopLoss(calculateStopLoss(order.getAveragePrice(), signal.getSignalType()));
        trade.setTakeProfit(calculateTakeProfit(order.getAveragePrice(), signal.getSignalType()));
        trade.setStatus(Trade.TradeStatus.OPEN);
        trade.setEntryTime(LocalDateTime.now());
        trade.setEntryOrderId(order.getOrderId());
        trade.setPaperTrading(order.getPaperTrading());

        tradeRepository.save(trade);

        updateStrategyStats(strategy, trade);
    }

    private BigDecimal calculateStopLoss(BigDecimal entryPrice, SuddenMovementStrategy.SignalType signalType) {
        double stopLossPercentage = 0.02;
        if (signalType == SuddenMovementStrategy.SignalType.BUY) {
            return entryPrice.multiply(BigDecimal.valueOf(1 - stopLossPercentage));
        } else {
            return entryPrice.multiply(BigDecimal.valueOf(1 + stopLossPercentage));
        }
    }

    private BigDecimal calculateTakeProfit(BigDecimal entryPrice, SuddenMovementStrategy.SignalType signalType) {
        double takeProfitPercentage = 0.04;
        if (signalType == SuddenMovementStrategy.SignalType.BUY) {
            return entryPrice.multiply(BigDecimal.valueOf(1 + takeProfitPercentage));
        } else {
            return entryPrice.multiply(BigDecimal.valueOf(1 - takeProfitPercentage));
        }
    }

    private void updateStrategyStats(Strategy strategy, Trade trade) {
        strategy.setTotalTrades((strategy.getTotalTrades() != null ? strategy.getTotalTrades() : 0) + 1);
        strategy.setLastExecuted(LocalDateTime.now());
        strategyRepository.save(strategy);
    }

    private List<String> getWatchedSymbols(Strategy strategy) {
        return Arrays.asList("256265", "738561", "341249");
    }

    private List<BigDecimal> getRecentPrices(String symbol, int count) {
        List<BigDecimal> prices = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                BigDecimal price = marketDataService.getCurrentPrice(symbol);
                prices.add(price);
            }
        } catch (Exception e) {
            log.error("Error getting recent prices", e);
        }
        return prices;
    }

    private List<Long> getRecentVolumes(String symbol, int count) {
        List<Long> volumes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            volumes.add(1000L);
        }
        return volumes;
    }

    private int getStrategyQuantity(Strategy strategy) {
        return 1;
    }

    public List<Trade> getOpenTrades() {
        return tradeRepository.findOpenTrades(Trade.TradeStatus.OPEN);
    }

    public void checkExitConditions() {
        List<Trade> openTrades = getOpenTrades();

        for (Trade trade : openTrades) {
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(trade.getSymbol());

                if (shouldExitTrade(trade, currentPrice)) {
                    closeTrade(trade, currentPrice);
                }
            } catch (Exception e) {
                log.error("Error checking exit conditions for trade: {}", trade.getTradeId(), e);
            }
        }
    }

    private boolean shouldExitTrade(Trade trade, BigDecimal currentPrice) {
        if (trade.getTradeType() == Trade.TradeType.LONG) {
            return currentPrice.compareTo(trade.getStopLoss()) <= 0 ||
                   currentPrice.compareTo(trade.getTakeProfit()) >= 0;
        } else {
            return currentPrice.compareTo(trade.getStopLoss()) >= 0 ||
                   currentPrice.compareTo(trade.getTakeProfit()) <= 0;
        }
    }

    private void closeTrade(Trade trade, BigDecimal exitPrice) {
        try {
            OrderService.OrderRequest orderRequest = new OrderService.OrderRequest();
            orderRequest.setSymbol(trade.getSymbol());
            orderRequest.setExchange(trade.getExchange());

            if (trade.getTradeType() == Trade.TradeType.LONG) {
                orderRequest.setTransactionType(Order.TransactionType.SELL);
            } else {
                orderRequest.setTransactionType(Order.TransactionType.BUY);
            }

            orderRequest.setOrderType(Order.OrderType.MARKET);
            orderRequest.setQuantity(trade.getQuantity());
            orderRequest.setRemarks("Exit trade: " + trade.getTradeId());

            Order exitOrder = orderService.placeOrder(orderRequest);

            if (exitOrder != null) {
                trade.setExitPrice(exitOrder.getAveragePrice());
                trade.setExitTime(LocalDateTime.now());
                trade.setExitOrderId(exitOrder.getOrderId());

                BigDecimal pnl = calculatePnL(trade);
                trade.setPnl(pnl);
                trade.setPnlPercentage(calculatePnLPercentage(trade));

                if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                    trade.setStatus(Trade.TradeStatus.PROFIT_TAKEN);
                } else {
                    trade.setStatus(Trade.TradeStatus.STOPPED_OUT);
                }

                tradeRepository.save(trade);
                log.info("Closed trade {} with P&L: {}", trade.getTradeId(), pnl);
            }
        } catch (Exception e) {
            log.error("Error closing trade: {}", trade.getTradeId(), e);
        }
    }

    private BigDecimal calculatePnL(Trade trade) {
        BigDecimal priceDiff;
        if (trade.getTradeType() == Trade.TradeType.LONG) {
            priceDiff = trade.getExitPrice().subtract(trade.getEntryPrice());
        } else {
            priceDiff = trade.getEntryPrice().subtract(trade.getExitPrice());
        }
        return priceDiff.multiply(BigDecimal.valueOf(trade.getQuantity()));
    }

    private BigDecimal calculatePnLPercentage(Trade trade) {
        BigDecimal priceDiff;
        if (trade.getTradeType() == Trade.TradeType.LONG) {
            priceDiff = trade.getExitPrice().subtract(trade.getEntryPrice());
        } else {
            priceDiff = trade.getEntryPrice().subtract(trade.getExitPrice());
        }
        return priceDiff.divide(trade.getEntryPrice(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    @Scheduled(fixedRate = 30000)
    public void monitorOpenPositions() {
        checkExitConditions();
    }
}
