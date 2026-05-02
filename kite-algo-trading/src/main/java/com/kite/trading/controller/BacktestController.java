package com.kite.trading.controller;

import com.kite.trading.service.BacktestingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BacktestController {

    private final BacktestingService backtestingService;

    @PostMapping("/run")
    public ResponseEntity<BacktestingService.BacktestResult> runBacktest(
            @RequestBody BacktestingService.BacktestRequest request) {
        try {
            BacktestingService.BacktestResult result = backtestingService.runBacktest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/run")
    public ResponseEntity<BacktestingService.BacktestResult> runBacktestWithParams(
            @RequestParam String symbol,
            @RequestParam String strategyName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "100000") Double initialCapital) {

        try {
            BacktestingService.BacktestRequest request = new BacktestingService.BacktestRequest();
            request.setSymbol(symbol);
            request.setStrategyName(strategyName);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setInitialCapital(new java.math.BigDecimal(initialCapital.toString()));

            BacktestingService.BacktestResult result = backtestingService.runBacktest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
