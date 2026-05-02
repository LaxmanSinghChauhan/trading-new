package com.kite.trading.controller;

import com.kite.trading.model.Strategy;
import com.kite.trading.model.Trade;
import com.kite.trading.repository.StrategyRepository;
import com.kite.trading.repository.TradeRepository;
import com.kite.trading.strategy.StrategyEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StrategyController {

    private final StrategyRepository strategyRepository;
    private final TradeRepository tradeRepository;
    private final StrategyEngine strategyEngine;

    @PostMapping
    public ResponseEntity<Strategy> createStrategy(@RequestBody Strategy strategy) {
        try {
            strategy.setStrategyId(java.util.UUID.randomUUID().toString());
            strategy.setCreatedAt(java.time.LocalDateTime.now());
            strategy.setLastExecuted(java.time.LocalDateTime.now());
            Strategy savedStrategy = strategyRepository.save(strategy);
            return ResponseEntity.ok(savedStrategy);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{strategyId}")
    public ResponseEntity<Strategy> getStrategy(@PathVariable String strategyId) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Strategy>> getAllStrategies() {
        List<Strategy> strategies = strategyRepository.findAll();
        return ResponseEntity.ok(strategies);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Strategy>> getActiveStrategies() {
        List<Strategy> strategies = strategyRepository.findByIsActiveTrue();
        return ResponseEntity.ok(strategies);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Strategy>> getStrategiesByType(@PathVariable Strategy.StrategyType type) {
        List<Strategy> strategies = strategyRepository.findByType(type);
        return ResponseEntity.ok(strategies);
    }

    @PutMapping("/{strategyId}")
    public ResponseEntity<Strategy> updateStrategy(@PathVariable String strategyId,
                                                 @RequestBody Strategy strategy) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(existingStrategy -> {
                    existingStrategy.setName(strategy.getName());
                    existingStrategy.setDescription(strategy.getDescription());
                    existingStrategy.setParameters(strategy.getParameters());
                    existingStrategy.setIsActive(strategy.getIsActive());
                    existingStrategy.setPaperTrading(strategy.getPaperTrading());
                    Strategy updated = strategyRepository.save(existingStrategy);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{strategyId}/activate")
    public ResponseEntity<Strategy> activateStrategy(@PathVariable String strategyId) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(strategy -> {
                    strategy.setIsActive(true);
                    Strategy updated = strategyRepository.save(strategy);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{strategyId}/deactivate")
    public ResponseEntity<Strategy> deactivateStrategy(@PathVariable String strategyId) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(strategy -> {
                    strategy.setIsActive(false);
                    Strategy updated = strategyRepository.save(strategy);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{strategyId}")
    public ResponseEntity<Void> deleteStrategy(@PathVariable String strategyId) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(strategy -> {
                    strategyRepository.delete(strategy);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{strategyId}/execute")
    public ResponseEntity<String> executeStrategy(@PathVariable String strategyId) {
        return strategyRepository.findByStrategyId(strategyId)
                .map(strategy -> {
                    try {
                        strategyEngine.executeStrategy(strategy);
                        return ResponseEntity.ok("Strategy executed successfully");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Strategy execution failed: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{strategyId}/trades")
    public ResponseEntity<List<Trade>> getStrategyTrades(@PathVariable String strategyId) {
        List<Trade> trades = tradeRepository.findByStrategyName(strategyId);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/trades/open")
    public ResponseEntity<List<Trade>> getOpenTrades() {
        List<Trade> trades = strategyEngine.getOpenTrades();
        return ResponseEntity.ok(trades);
    }

    @PostMapping("/trades/check-exit")
    public ResponseEntity<String> checkExitConditions() {
        try {
            strategyEngine.checkExitConditions();
            return ResponseEntity.ok("Exit conditions checked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exit check failed: " + e.getMessage());
        }
    }
}
