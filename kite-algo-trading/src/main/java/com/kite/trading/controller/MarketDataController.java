package com.kite.trading.controller;

import com.kite.trading.model.MarketData;
import com.kite.trading.service.MarketDataService;
import com.kite.trading.service.MockKiteConnect;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<MockKiteConnect.MockQuote> getLiveQuote(@PathVariable String symbol) {
        try {
            MockKiteConnect.MockQuote quote = marketDataService.getLiveQuote(symbol);
            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/quotes")
    public ResponseEntity<Map<String, MockKiteConnect.MockQuote>> getLiveQuotes(@RequestParam List<String> symbols) {
        try {
            Map<String, MockKiteConnect.MockQuote> quotes = marketDataService.getLiveQuotes(symbols);
            return ResponseEntity.ok(quotes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/historical/{symbol}")
    public ResponseEntity<List<MarketData>> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "day") String interval) {
        try {
            Date fromDate = java.sql.Timestamp.valueOf(from);
            Date toDate = java.sql.Timestamp.valueOf(to);
            List<MarketData> data = marketDataService.getHistoricalData(symbol, fromDate, toDate, interval);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/historical/db/{symbol}")
    public ResponseEntity<List<MarketData>> getHistoricalDataFromDB(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            List<MarketData> data = marketDataService.getHistoricalDataFromDB(symbol, from, to);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/latest/{symbol}")
    public ResponseEntity<MarketData> getLatestData(@PathVariable String symbol) {
        MarketData data = marketDataService.getLatestData(symbol);
        if (data != null) {
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/latest")
    public ResponseEntity<List<MarketData>> getLatestData(@RequestParam List<String> symbols) {
        List<MarketData> data = marketDataService.getLatestData(symbols);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/price/{symbol}")
    public ResponseEntity<BigDecimal> getCurrentPrice(@PathVariable String symbol) {
        try {
            BigDecimal price = marketDataService.getCurrentPrice(symbol);
            return ResponseEntity.ok(price);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/change/{symbol}")
    public ResponseEntity<BigDecimal> getDayChange(@PathVariable String symbol) {
        try {
            BigDecimal change = marketDataService.getDayChange(symbol);
            return ResponseEntity.ok(change);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/change-percent/{symbol}")
    public ResponseEntity<BigDecimal> getDayChangePercentage(@PathVariable String symbol) {
        try {
            BigDecimal changePercent = marketDataService.getDayChangePercentage(symbol);
            return ResponseEntity.ok(changePercent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
