package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.SystemConfigEntry;
import com.kite.trading.algo.repository.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("strategy_direction", "LONG_ONLY"),
            Map.entry("trading_enabled", "true"),
            Map.entry("max_daily_capital", "100000"),
            Map.entry("max_open_positions", "5"),
            Map.entry("max_capital_per_trade_pct", "10.0"),
            Map.entry("max_daily_loss_pct", "3.0"),
            Map.entry("stop_loss_pct", "0.8"),
            Map.entry("target1_pct", "1.0"),
            Map.entry("target2_pct", "2.0"),
            Map.entry("trailing_stop_activation_pct", "1.5"),
            Map.entry("trailing_stop_distance_pct", "0.5"),
            Map.entry("signal_price_change_pct", "0.5"),
            Map.entry("signal_time_window_sec", "30"),
            Map.entry("signal_volume_multiplier", "2.0"),
            Map.entry("signal_confirmation_ticks", "3"),
            Map.entry("signal_min_ticks", "10"),
            Map.entry("signal_cooldown_sec", "60"),
            Map.entry("market_fall_threshold_pct", "1.5")
    );

    private final SystemConfigRepository repository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    @Transactional
    public void initialize() {
        DEFAULTS.forEach((key, value) -> repository.findById(key).orElseGet(() -> {
            SystemConfigEntry entry = new SystemConfigEntry();
            entry.setKey(key);
            entry.setValue(value);
            entry.setUpdatedAt(LocalDateTime.now());
            return repository.save(entry);
        }));
        reload();
    }

    public void reload() {
        cache.clear();
        repository.findAll().forEach(entry -> cache.put(entry.getKey(), entry.getValue()));
        log.info("Loaded {} runtime config values", cache.size());
    }

    public Map<String, String> snapshot() {
        return new LinkedHashMap<>(cache);
    }

    @Transactional
    public Map<String, String> update(Map<String, String> updates) {
        LocalDateTime now = LocalDateTime.now();
        updates.forEach((key, value) -> {
            SystemConfigEntry entry = repository.findById(key).orElseGet(SystemConfigEntry::new);
            entry.setKey(key);
            entry.setValue(value);
            entry.setUpdatedAt(now);
            repository.save(entry);
            cache.put(key, value);
        });
        return snapshot();
    }

    public boolean tradingEnabled() {
        return getBoolean("trading_enabled");
    }

    public int maxOpenPositions() {
        return getInt("max_open_positions");
    }

    public BigDecimal maxDailyCapital() {
        return getDecimal("max_daily_capital");
    }

    public BigDecimal maxCapitalPerTradePct() {
        return getDecimal("max_capital_per_trade_pct");
    }

    public BigDecimal maxDailyLossPct() {
        return getDecimal("max_daily_loss_pct");
    }

    public BigDecimal stopLossPct() {
        return getDecimal("stop_loss_pct");
    }

    public BigDecimal target1Pct() {
        return getDecimal("target1_pct");
    }

    public BigDecimal target2Pct() {
        return getDecimal("target2_pct");
    }

    public BigDecimal trailingActivationPct() {
        return getDecimal("trailing_stop_activation_pct");
    }

    public BigDecimal trailingDistancePct() {
        return getDecimal("trailing_stop_distance_pct");
    }

    public BigDecimal signalPriceChangePct() {
        return getDecimal("signal_price_change_pct");
    }

    public int signalWindowSeconds() {
        return getInt("signal_time_window_sec");
    }

    public BigDecimal signalVolumeMultiplier() {
        return getDecimal("signal_volume_multiplier");
    }

    public int signalConfirmationTicks() {
        return getInt("signal_confirmation_ticks");
    }

    public int signalMinTicks() {
        return getInt("signal_min_ticks");
    }

    public int signalCooldownSeconds() {
        return getInt("signal_cooldown_sec");
    }

    public BigDecimal marketFallThresholdPct() {
        return getDecimal("market_fall_threshold_pct");
    }

    public String strategyDirection() {
        return cache.getOrDefault("strategy_direction", "LONG_ONLY");
    }

    public String get(String key) {
        return cache.get(key);
    }

    private boolean getBoolean(String key) {
        return Boolean.parseBoolean(cache.getOrDefault(key, DEFAULTS.get(key)));
    }

    private int getInt(String key) {
        return Integer.parseInt(cache.getOrDefault(key, DEFAULTS.get(key)));
    }

    private BigDecimal getDecimal(String key) {
        return new BigDecimal(cache.getOrDefault(key, DEFAULTS.get(key)));
    }
}
