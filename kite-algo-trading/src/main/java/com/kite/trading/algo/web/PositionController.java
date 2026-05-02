package com.kite.trading.algo.web;

import com.kite.trading.algo.repository.TradeLegLogRepository;
import com.kite.trading.algo.repository.TradeLogRepository;
import com.kite.trading.algo.service.PositionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionManager positionManager;
    private final TradeLogRepository tradeLogRepository;
    private final TradeLegLogRepository tradeLegLogRepository;

    @GetMapping
    public ResponseEntity<?> positions() {
        return ResponseEntity.ok(positionManager.snapshots());
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(tradeLogRepository.findTop50ByOrderByEntryTimeDesc().stream()
                .map(trade -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("trade", trade);
                    payload.put("legs", tradeLegLogRepository.findByTradeIdOrderByExecutedAtAsc(trade.getId()));
                    return payload;
                })
                .toList());
    }

    @PostMapping("/{symbol}/exit")
    public ResponseEntity<Map<String, String>> exitPosition(@PathVariable String symbol) {
        positionManager.exitPosition(symbol.toUpperCase(), "MANUAL_EXIT");
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }
}
