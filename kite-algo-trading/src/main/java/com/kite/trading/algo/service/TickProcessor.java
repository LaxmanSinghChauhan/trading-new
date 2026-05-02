package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.Signal;
import com.kite.trading.algo.runtime.Tick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickProcessor {

    private final TickDataStore tickDataStore;
    private final StockUniverseService stockUniverseService;
    private final MarketHealthMonitor marketHealthMonitor;
    private final PositionManager positionManager;
    private final SignalDetectionEngine signalDetectionEngine;
    private final SignalBus signalBus;
    private final RecentSignalStore recentSignalStore;

    public void process(Tick tick) {
        if (!stockUniverseService.isTracked(tick.instrumentToken())) {
            return;
        }

        tickDataStore.append(tick);
        marketHealthMonitor.onTick(tick);
        positionManager.onTick(tick);

        if (!stockUniverseService.isTradeable(tick.symbol())) {
            return;
        }

        signalDetectionEngine.detect(tick).ifPresent(this::publishSignal);
    }

    private void publishSignal(Signal signal) {
        recentSignalStore.add(signal);
        signalBus.offer(signal);
        log.info("Detected signal {} at {}", signal.symbol(), signal.lastPrice());
    }
}
