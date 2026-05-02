package com.kite.trading.algo.service;

import com.kite.trading.algo.config.MarketProperties;
import com.kite.trading.algo.runtime.Signal;
import com.kite.trading.algo.runtime.SignalStrength;
import com.kite.trading.algo.runtime.Tick;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SignalDetectionEngine {

    private final TickDataStore tickDataStore;
    private final MomentumIndicator momentumIndicator;
    private final SystemConfigService systemConfigService;
    private final MarketProperties marketProperties;
    private final Clock tradingClock;
    private final Map<String, LocalDateTime> cooldowns = new ConcurrentHashMap<>();

    public Optional<Signal> detect(Tick tick) {
        LocalTime now = LocalDateTime.now(tradingClock).toLocalTime();
        if (now.isBefore(marketProperties.getHoursStart()) || now.isAfter(marketProperties.getSignalCutoff())) {
            return Optional.empty();
        }

        List<Tick> ticks = tickDataStore.getTicks(tick.instrumentToken());
        if (ticks.size() < systemConfigService.signalMinTicks()) {
            return Optional.empty();
        }

        LocalDateTime nextEligibleTime = cooldowns.get(tick.symbol());
        if (nextEligibleTime != null && nextEligibleTime.isAfter(LocalDateTime.now(tradingClock))) {
            return Optional.empty();
        }

        BigDecimal accelerationPct = momentumIndicator.priceAccelerationPct(ticks, systemConfigService.signalWindowSeconds());
        if (accelerationPct.compareTo(systemConfigService.signalPriceChangePct()) < 0) {
            return Optional.empty();
        }

        BigDecimal volumeMultiplier = momentumIndicator.volumeMultiplier(ticks);
        if (volumeMultiplier.compareTo(systemConfigService.signalVolumeMultiplier()) < 0) {
            return Optional.empty();
        }

        if (!momentumIndicator.hasConsecutiveHigherTicks(ticks, systemConfigService.signalConfirmationTicks())) {
            return Optional.empty();
        }
        if (!momentumIndicator.microTrendUp(ticks)) {
            return Optional.empty();
        }
        if (momentumIndicator.isNoisySpike(ticks)) {
            return Optional.empty();
        }

        SignalStrength strength = momentumIndicator.classifyStrength(accelerationPct);
        LocalDateTime detectedAt = LocalDateTime.now(tradingClock);
        cooldowns.put(tick.symbol(), detectedAt.plusSeconds(systemConfigService.signalCooldownSeconds()));
        return Optional.of(new Signal(
                tick.symbol(),
                tick.instrumentToken(),
                tick.lastPrice(),
                accelerationPct,
                volumeMultiplier,
                strength,
                detectedAt
        ));
    }
}
