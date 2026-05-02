package com.kite.trading.service;

import com.kite.trading.model.MarketData;
import com.kite.trading.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final KiteConnectService kiteConnectService;
    private final MarketDataRepository marketDataRepository;

    public MockKiteConnect.MockQuote getLiveQuote(String symbol) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }
        return kiteConnectService.getQuote(symbol);
    }

    public Map<String, MockKiteConnect.MockQuote> getLiveQuotes(List<String> symbols) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }
        return kiteConnectService.getQuotes(symbols);
    }

    @Cacheable(value = "historicalData", key = "#symbol + #from + #to + #interval")
    public List<MarketData> getHistoricalData(String symbol, Date from, Date to, String interval) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }

        List<MockKiteConnect.MockHistoricalData> historicalDataList =
                kiteConnectService.getHistoricalData(symbol, from, to, interval);

        return historicalDataList.stream()
                .map(this::convertToMarketData)
                .collect(Collectors.toList());
    }

    public List<MarketData> getHistoricalDataFromDB(String symbol, LocalDateTime from, LocalDateTime to) {
        return marketDataRepository.findBySymbolAndDateRange(symbol, from, to);
    }

    public MarketData getLatestData(String symbol) {
        return marketDataRepository.findLatestBySymbol(symbol);
    }

    public List<MarketData> getLatestData(List<String> symbols) {
        List<MarketData> result = new ArrayList<>();
        for (String symbol : symbols) {
            MarketData data = marketDataRepository.findLatestBySymbol(symbol);
            if (data != null) {
                result.add(data);
            }
        }
        return result;
    }

    @Scheduled(fixedRate = 60000)
    public void fetchAndStoreLiveQuotes() {
        try {
            if (!kiteConnectService.isInitialized()) {
                log.warn("Kite Connect not initialized, skipping live quote fetch");
                return;
            }

            List<String> watchedSymbols = getWatchedSymbols();
            if (watchedSymbols.isEmpty()) {
                return;
            }

            Map<String, MockKiteConnect.MockQuote> quotes = kiteConnectService.getQuotes(watchedSymbols);

            for (Map.Entry<String, MockKiteConnect.MockQuote> entry : quotes.entrySet()) {
                MockKiteConnect.MockQuote quote = entry.getValue();
                MarketData marketData = convertQuoteToMarketData(entry.getKey(), quote);
                marketDataRepository.save(marketData);
            }

            log.info("Fetched and stored {} live quotes", quotes.size());
        } catch (Exception e) {
            log.error("Error fetching live quotes", e);
        }
    }

    public void storeHistoricalData(String symbol, List<MarketData> data) {
        marketDataRepository.saveAll(data);
        log.info("Stored {} historical data points for symbol {}", data.size(), symbol);
    }

    public BigDecimal getCurrentPrice(String symbol) throws Exception {
        MockKiteConnect.MockQuote quote = getLiveQuote(symbol);
        return quote.getLastPrice();
    }

    public BigDecimal getPreviousClose(String symbol) throws Exception {
        MockKiteConnect.MockQuote quote = getLiveQuote(symbol);
        return quote.getClosePrice();
    }

    public BigDecimal getDayChange(String symbol) throws Exception {
        MockKiteConnect.MockQuote quote = getLiveQuote(symbol);
        return quote.getLastPrice().subtract(quote.getClosePrice());
    }

    public BigDecimal getDayChangePercentage(String symbol) throws Exception {
        MockKiteConnect.MockQuote quote = getLiveQuote(symbol);
        if (quote.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return quote.getLastPrice().subtract(quote.getClosePrice())
                .divide(quote.getClosePrice(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private MarketData convertToMarketData(MockKiteConnect.MockHistoricalData historicalData) {
        MarketData marketData = new MarketData();
        marketData.setTimestamp(convertToLocalDateTime(historicalData.getTimeStamp()));
        marketData.setOpen(new BigDecimal(historicalData.getOpen()));
        marketData.setHigh(new BigDecimal(historicalData.getHigh()));
        marketData.setLow(new BigDecimal(historicalData.getLow()));
        marketData.setClose(new BigDecimal(historicalData.getClose()));
        marketData.setVolume(historicalData.getVolume());
        return marketData;
    }

    private MarketData convertQuoteToMarketData(String symbol, MockKiteConnect.MockQuote quote) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol);
        marketData.setTimestamp(LocalDateTime.now());
        marketData.setOpen(quote.getOhlc().getOpen());
        marketData.setHigh(quote.getOhlc().getHigh());
        marketData.setLow(quote.getOhlc().getLow());
        marketData.setClose(quote.getLastPrice());
        marketData.setVolume(quote.getTotalBuyQuantity() + quote.getTotalSellQuantity());
        marketData.setVwap(quote.getAverageTradePrice());
        marketData.setDataInterval("minute");
        return marketData;
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private List<String> getWatchedSymbols() {
        return Arrays.asList("256265", "738561", "341249");
    }
}
