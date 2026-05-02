package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.Signal;
import com.kite.trading.algo.runtime.SignalStrength;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RecentSignalStore {

    private static final int MAX_SIGNALS = 200;

    private final List<Signal> signals = new ArrayList<>();

    public synchronized void add(Signal signal) {
        signals.add(signal);
        signals.sort(Comparator.comparing(Signal::detectedAt));
        if (signals.size() > MAX_SIGNALS) {
            signals.remove(0);
        }
    }

    public synchronized List<Signal> recent(int limit) {
        return signals.stream()
                .sorted(Comparator.comparing(Signal::detectedAt).reversed())
                .limit(limit)
                .toList();
    }

    public synchronized Map<String, Object> stats() {
        Map<SignalStrength, Long> byStrength = new EnumMap<>(SignalStrength.class);
        for (SignalStrength strength : SignalStrength.values()) {
            byStrength.put(strength, 0L);
        }
        signals.forEach(signal -> byStrength.computeIfPresent(signal.strength(), (strength, count) -> count + 1));
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalSignals", signals.size());
        stats.put("lastSignalAt", signals.isEmpty() ? null : recent(1).get(0).detectedAt());
        stats.put("signalsToday", signals.stream().filter(signal -> signal.detectedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate())).count());
        stats.put("byStrength", byStrength);
        return stats;
    }
}
