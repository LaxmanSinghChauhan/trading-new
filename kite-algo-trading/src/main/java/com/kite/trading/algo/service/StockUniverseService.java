package com.kite.trading.algo.service;

import com.kite.trading.algo.config.UniverseProperties;
import com.kite.trading.algo.runtime.InstrumentMasterRecord;
import com.kite.trading.algo.runtime.UniverseStock;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockUniverseService {

    private final UniverseProperties universeProperties;
    private final InstrumentMasterService instrumentMasterService;

    private final Map<String, UniverseStock> activeBySymbol = new ConcurrentHashMap<>();
    private final Map<Long, UniverseStock> activeByToken = new ConcurrentHashMap<>();
    private final Set<String> runtimeBlacklist = ConcurrentHashMap.newKeySet();

    @PostConstruct
    void init() {
        runtimeBlacklist.addAll(normalizeSymbols(universeProperties.getBlacklist()));
        refreshUniverse();
    }

    @Scheduled(cron = "${universe.refresh-cron:0 0 8 * * MON-FRI}", zone = "Asia/Kolkata")
    public synchronized void refreshUniverse() {
        Set<String> targetSymbols = new LinkedHashSet<>();
        targetSymbols.addAll(loadSymbols(universeProperties.getNifty500Resource()));
        targetSymbols.addAll(loadSymbols(universeProperties.getBankniftyResource()));

        Map<String, UniverseStock> updatedBySymbol = new LinkedHashMap<>();
        Map<Long, UniverseStock> updatedByToken = new LinkedHashMap<>();
        for (InstrumentMasterRecord record : instrumentMasterService.loadInstrumentMaster()) {
            if (!"NSE".equals(record.exchange())) {
                continue;
            }
            if (!"EQ".equals(record.instrumentType()) && record.instrumentToken() != MarketHealthMonitor.NIFTY_50_TOKEN) {
                continue;
            }
            if (record.instrumentToken() != MarketHealthMonitor.NIFTY_50_TOKEN) {
                if (!targetSymbols.contains(record.symbol())) {
                    continue;
                }
                if (record.lastPrice().compareTo(universeProperties.getMinPrice()) < 0) {
                    continue;
                }
                if (runtimeBlacklist.contains(record.symbol())) {
                    continue;
                }
            }
            UniverseStock stock = new UniverseStock(
                    record.symbol(),
                    record.instrumentToken(),
                    record.exchange(),
                    record.instrumentType(),
                    record.lastPrice(),
                    targetSymbols.contains(record.symbol()) ? "UNIVERSE_FILE" : "SYSTEM"
            );
            updatedBySymbol.put(stock.symbol(), stock);
            updatedByToken.put(stock.instrumentToken(), stock);
        }

        if (!updatedByToken.containsKey(MarketHealthMonitor.NIFTY_50_TOKEN)) {
            UniverseStock nifty = new UniverseStock("NIFTY_50", MarketHealthMonitor.NIFTY_50_TOKEN, "NSE", "INDEX", universeProperties.getMinPrice(), "SYSTEM");
            updatedBySymbol.put(nifty.symbol(), nifty);
            updatedByToken.put(nifty.instrumentToken(), nifty);
        }

        activeBySymbol.clear();
        activeBySymbol.putAll(updatedBySymbol);
        activeByToken.clear();
        activeByToken.putAll(updatedByToken);
        log.info("Loaded active universe with {} instruments", activeBySymbol.size());
    }

    public synchronized List<UniverseStock> activeUniverse() {
        return activeBySymbol.values().stream()
                .sorted(java.util.Comparator.comparing(UniverseStock::symbol))
                .toList();
    }

    public synchronized void blacklist(String symbol) {
        String normalized = symbol.trim().toUpperCase();
        runtimeBlacklist.add(normalized);
        activeBySymbol.remove(normalized);
        activeByToken.entrySet().removeIf(entry -> normalized.equals(entry.getValue().symbol()));
    }

    public boolean isTracked(long token) {
        return activeByToken.containsKey(token);
    }

    public boolean isTradeable(String symbol) {
        UniverseStock stock = activeBySymbol.get(symbol);
        return stock != null && !"INDEX".equals(stock.instrumentType());
    }

    public UniverseStock byToken(long token) {
        return activeByToken.get(token);
    }

    public Set<String> blacklistedSymbols() {
        return new LinkedHashSet<>(runtimeBlacklist);
    }

    private Set<String> loadSymbols(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            return Set.of();
        }
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            List<String> lines = new java.io.BufferedReader(reader).lines().toList();
            return normalizeSymbols(lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read resource " + resourcePath, exception);
        }
    }

    private Set<String> normalizeSymbols(List<String> symbols) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String line : symbols) {
            if (line == null) {
                continue;
            }
            String symbol = line.trim().toUpperCase();
            if (symbol.isEmpty() || symbol.startsWith("#")) {
                continue;
            }
            normalized.add(symbol);
        }
        return normalized;
    }
}
