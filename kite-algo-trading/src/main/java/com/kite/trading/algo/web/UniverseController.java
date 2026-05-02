package com.kite.trading.algo.web;

import com.kite.trading.algo.service.StockUniverseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/universe")
@RequiredArgsConstructor
public class UniverseController {

    private final StockUniverseService stockUniverseService;

    @GetMapping
    public ResponseEntity<?> universe() {
        return ResponseEntity.ok(Map.of(
                "active", stockUniverseService.activeUniverse(),
                "blacklist", stockUniverseService.blacklistedSymbols()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        stockUniverseService.refreshUniverse();
        return ResponseEntity.ok(Map.of("status", "refreshed", "count", stockUniverseService.activeUniverse().size()));
    }

    @PostMapping("/blacklist/{symbol}")
    public ResponseEntity<?> blacklist(@PathVariable String symbol) {
        stockUniverseService.blacklist(symbol);
        return ResponseEntity.ok(Map.of("status", "blacklisted", "symbol", symbol.toUpperCase()));
    }
}
