package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.Tick;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TickDataStore {

    private static final int MAX_TICKS_PER_SYMBOL = 120;

    private final Map<Long, Deque<Tick>> history = new ConcurrentHashMap<>();
    private final Map<Long, Tick> latestTicks = new ConcurrentHashMap<>();

    public void append(Tick tick) {
        Deque<Tick> deque = history.computeIfAbsent(tick.instrumentToken(), ignored -> new ArrayDeque<>());
        synchronized (deque) {
            if (deque.size() >= MAX_TICKS_PER_SYMBOL) {
                deque.removeFirst();
            }
            deque.addLast(tick);
        }
        latestTicks.put(tick.instrumentToken(), tick);
    }

    public List<Tick> getTicks(long token) {
        Deque<Tick> deque = history.get(token);
        if (deque == null) {
            return List.of();
        }
        synchronized (deque) {
            return new ArrayList<>(deque);
        }
    }

    public Optional<Tick> getLatestTick(long token) {
        return Optional.ofNullable(latestTicks.get(token));
    }
}
