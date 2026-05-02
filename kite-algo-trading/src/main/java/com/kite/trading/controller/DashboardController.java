package com.kite.trading.controller;

import com.kite.trading.model.Order;
import com.kite.trading.model.Trade;
import com.kite.trading.repository.OrderRepository;
import com.kite.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING).size();
        long completedOrders = orderRepository.findByStatus(Order.OrderStatus.COMPLETE).size();

        long totalTrades = tradeRepository.count();
        long openTrades = tradeRepository.findByStatusAndPaperTradingFalse(Trade.TradeStatus.OPEN).size();
        long winningTrades = tradeRepository.findAll().stream()
                .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) > 0)
                .count();

        BigDecimal totalPnl = tradeRepository.findAll().stream()
                .filter(t -> t.getPnl() != null)
                .map(Trade::getPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal winRate = totalTrades > 0 ?
                BigDecimal.valueOf(winningTrades)
                        .divide(BigDecimal.valueOf(totalTrades), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

        overview.put("totalOrders", totalOrders);
        overview.put("pendingOrders", pendingOrders);
        overview.put("completedOrders", completedOrders);
        overview.put("totalTrades", totalTrades);
        overview.put("openTrades", openTrades);
        overview.put("winningTrades", winningTrades);
        overview.put("totalPnl", totalPnl);
        overview.put("winRate", winRate);
        overview.put("lastUpdated", LocalDateTime.now());

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        List<Order> orders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent-trades")
    public ResponseEntity<List<Trade>> getRecentTrades(@RequestParam(defaultValue = "10") int limit) {
        List<Trade> trades = tradeRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getEntryTime().compareTo(t1.getEntryTime()))
                .limit(limit)
                .toList();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/open-positions")
    public ResponseEntity<List<Trade>> getOpenPositions() {
        List<Trade> trades = tradeRepository.findOpenTrades(Trade.TradeStatus.OPEN);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> performance = new HashMap<>();

        List<Trade> allTrades = tradeRepository.findAll();

        if (!allTrades.isEmpty()) {
            BigDecimal totalPnl = allTrades.stream()
                    .filter(t -> t.getPnl() != null)
                    .map(Trade::getPnl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long winningTrades = allTrades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) > 0)
                    .count();

            long losingTrades = allTrades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) < 0)
                    .count();

            BigDecimal winRate = BigDecimal.valueOf(winningTrades)
                    .divide(BigDecimal.valueOf(allTrades.size()), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            BigDecimal avgWin = allTrades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) > 0)
                    .map(Trade::getPnl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(winningTrades, 1)), 4, java.math.RoundingMode.HALF_UP);

            BigDecimal avgLoss = allTrades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) < 0)
                    .map(Trade::getPnl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(losingTrades, 1)), 4, java.math.RoundingMode.HALF_UP);

            BigDecimal profitFactor = avgLoss.compareTo(BigDecimal.ZERO) != 0 ?
                    avgWin.divide(avgLoss.abs(), 4, java.math.RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            performance.put("totalPnl", totalPnl);
            performance.put("winRate", winRate);
            performance.put("winningTrades", winningTrades);
            performance.put("losingTrades", losingTrades);
            performance.put("avgWin", avgWin);
            performance.put("avgLoss", avgLoss);
            performance.put("profitFactor", profitFactor);
            performance.put("totalTrades", allTrades.size());
        } else {
            performance.put("totalPnl", BigDecimal.ZERO);
            performance.put("winRate", BigDecimal.ZERO);
            performance.put("winningTrades", 0L);
            performance.put("losingTrades", 0L);
            performance.put("avgWin", BigDecimal.ZERO);
            performance.put("avgLoss", BigDecimal.ZERO);
            performance.put("profitFactor", BigDecimal.ZERO);
            performance.put("totalTrades", 0L);
        }

        return ResponseEntity.ok(performance);
    }

    @GetMapping("/symbol-performance/{symbol}")
    public ResponseEntity<Map<String, Object>> getSymbolPerformance(@PathVariable String symbol) {
        Map<String, Object> performance = new HashMap<>();

        List<Trade> symbolTrades = tradeRepository.findBySymbol(symbol);

        if (!symbolTrades.isEmpty()) {
            BigDecimal totalPnl = symbolTrades.stream()
                    .filter(t -> t.getPnl() != null)
                    .map(Trade::getPnl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long winningTrades = symbolTrades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) > 0)
                    .count();

            BigDecimal winRate = BigDecimal.valueOf(winningTrades)
                    .divide(BigDecimal.valueOf(symbolTrades.size()), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            performance.put("symbol", symbol);
            performance.put("totalTrades", symbolTrades.size());
            performance.put("totalPnl", totalPnl);
            performance.put("winRate", winRate);
            performance.put("winningTrades", winningTrades);
        } else {
            performance.put("symbol", symbol);
            performance.put("totalTrades", 0);
            performance.put("totalPnl", BigDecimal.ZERO);
            performance.put("winRate", BigDecimal.ZERO);
            performance.put("winningTrades", 0);
        }

        return ResponseEntity.ok(performance);
    }
}
