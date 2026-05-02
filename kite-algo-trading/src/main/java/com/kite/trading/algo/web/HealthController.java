package com.kite.trading.algo.web;

import com.kite.trading.algo.config.BrokerProperties;
import com.kite.trading.algo.service.BrokerGatewayResolver;
import com.kite.trading.algo.service.ExecutionQueue;
import com.kite.trading.algo.service.StockUniverseService;
import com.kite.trading.algo.service.SystemConfigService;
import com.kite.trading.algo.service.TickPipeline;
import com.kite.trading.service.KiteAuthService;
import com.kite.trading.service.KiteConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final BrokerProperties brokerProperties;
    private final BrokerGatewayResolver brokerGatewayResolver;
    private final KiteConnectService kiteConnectService;
    private final KiteAuthService kiteAuthService;
    private final SystemConfigService systemConfigService;
    private final TickPipeline tickPipeline;
    private final ExecutionQueue executionQueue;
    private final StockUniverseService stockUniverseService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("brokerMode", brokerProperties.getMode());
        payload.put("brokerConnected", brokerGatewayResolver.current().isConnected());
        payload.put("kiteConnected", kiteConnectService.isInitialized());
        payload.put("kiteAuthenticated", kiteAuthService.isAuthenticated());
        payload.put("tradingEnabled", systemConfigService.tradingEnabled());
        payload.put("tickQueueDepth", tickPipeline.depth());
        payload.put("executionQueueDepth", executionQueue.depth());
        payload.put("activeUniverseSize", stockUniverseService.activeUniverse().size());
        payload.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(payload);
    }
}
