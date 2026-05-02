package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.Tick;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class TickerService {

    private final TickPipeline tickPipeline;
    private final Clock tradingClock;

    public TickerService(TickPipeline tickPipeline, Clock tradingClock) {
        this.tickPipeline = tickPipeline;
        this.tradingClock = tradingClock;
    }

    public boolean ingest(Tick tick) {
        return tickPipeline.offer(tick);
    }

    public boolean ingest(long instrumentToken, String symbol, BigDecimal price, long volume) {
        return ingest(new Tick(instrumentToken, symbol, price, volume, LocalDateTime.now(tradingClock)));
    }
}
