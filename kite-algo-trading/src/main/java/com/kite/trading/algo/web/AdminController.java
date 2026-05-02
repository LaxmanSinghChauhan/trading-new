package com.kite.trading.algo.web;

import com.kite.trading.algo.repository.NewsLogRepository;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.service.BrokerGatewayResolver;
import com.kite.trading.algo.service.DailySummaryService;
import com.kite.trading.algo.service.ExecutionQueue;
import com.kite.trading.algo.service.PositionManager;
import com.kite.trading.algo.service.RecentSignalStore;
import com.kite.trading.algo.service.RiskEngine;
import com.kite.trading.algo.service.StockUniverseService;
import com.kite.trading.algo.service.SystemConfigService;
import com.kite.trading.algo.service.TickPipeline;
import com.kite.trading.algo.service.TickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SystemConfigService systemConfigService;
    private final PositionManager positionManager;
    private final RiskEngine riskEngine;
    private final RecentSignalStore recentSignalStore;
    private final NewsLogRepository newsLogRepository;
    private final DailySummaryService dailySummaryService;
    private final TickPipeline tickPipeline;
    private final ExecutionQueue executionQueue;
    private final StockUniverseService stockUniverseService;
    private final TickerService tickerService;
    private final BrokerGatewayResolver brokerGatewayResolver;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> config() {
        return ResponseEntity.ok(systemConfigService.snapshot());
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, String>> updateConfig(@RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(systemConfigService.update(updates));
    }

    @PostMapping("/trading/pause")
    public ResponseEntity<Map<String, String>> pauseTrading() {
        return ResponseEntity.ok(systemConfigService.update(Map.of("trading_enabled", "false")));
    }

    @PostMapping("/trading/resume")
    public ResponseEntity<Map<String, String>> resumeTrading() {
        return ResponseEntity.ok(systemConfigService.update(Map.of("trading_enabled", "true")));
    }

    @PostMapping("/exit-all")
    public ResponseEntity<Map<String, String>> exitAll() {
        positionManager.exitAll("MANUAL_EXIT");
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("config", systemConfigService.snapshot());
        payload.put("risk", riskEngine.status());
        payload.put("signals", recentSignalStore.stats());
        payload.put("openPositions", positionManager.snapshots());
        payload.put("latestNews", newsLogRepository.findTop50ByOrderByProcessedAtDesc().stream().limit(10).toList());
        payload.put("dailySummary", dailySummaryService.currentSummary().orElse(null));
        payload.put("queueDepths", Map.of(
                "tickPipeline", tickPipeline.depth(),
                "executionQueue", executionQueue.depth()
        ));
        payload.put("universeSize", stockUniverseService.activeUniverse().size());
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/news/recent")
    public ResponseEntity<?> recentNews() {
        return ResponseEntity.ok(newsLogRepository.findTop50ByOrderByProcessedAtDesc());
    }

    @GetMapping("/broker-mode")
    public ResponseEntity<Map<String, Object>> brokerMode() {
        return ResponseEntity.ok(Map.of(
                "activeMode", brokerGatewayResolver.mode().name(),
                "runtimeSwitchAllowed", false,
                "message", "Broker mode changes require an application restart."
        ));
    }

    @PostMapping("/broker-mode")
    public ResponseEntity<Map<String, Object>> requestBrokerModeChange(@RequestBody BrokerModeRequest request) {
        return ResponseEntity.status(409).body(Map.of(
                "activeMode", brokerGatewayResolver.mode().name(),
                "requestedMode", request.mode(),
                "runtimeSwitchAllowed", false,
                "message", "Runtime broker mode switching is disabled. Restart the app with BROKER_MODE=" + request.mode() + "."
        ));
    }

    @PostMapping("/ticks")
    public ResponseEntity<Map<String, Object>> simulateTick(@RequestBody SimulatedTickRequest request) {
        boolean accepted = tickerService.ingest(new Tick(
                request.instrumentToken(),
                request.symbol().toUpperCase(),
                request.lastPrice(),
                request.volume(),
                request.tickTime() != null ? request.tickTime() : LocalDateTime.now()
        ));
        return ResponseEntity.ok(Map.of("accepted", accepted));
    }

    public record SimulatedTickRequest(
            long instrumentToken,
            String symbol,
            BigDecimal lastPrice,
            long volume,
            LocalDateTime tickTime
    ) {
    }

    public record BrokerModeRequest(String mode) {
    }
}
