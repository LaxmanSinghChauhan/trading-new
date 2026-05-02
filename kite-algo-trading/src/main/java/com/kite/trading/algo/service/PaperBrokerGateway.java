package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.BrokerMode;
import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.runtime.TradeIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaperBrokerGateway implements BrokerGateway {

    private final TickDataStore tickDataStore;
    private final Clock tradingClock;

    @Override
    public BrokerOrderResult placeBuy(TradeIntent intent) {
        Tick latestTick = tickDataStore.getLatestTick(intent.instrumentToken())
                .orElseThrow(() -> new IllegalStateException("No latest tick for " + intent.symbol()));
        return new BrokerOrderResult(orderId(), latestTick.lastPrice(), intent.quantity(), LocalDateTime.now(tradingClock));
    }

    @Override
    public BrokerOrderResult placeSell(String symbol, long token, int quantity, String reason) {
        Tick latestTick = tickDataStore.getLatestTick(token)
                .orElseThrow(() -> new IllegalStateException("No latest tick for " + symbol));
        return new BrokerOrderResult(orderId(), latestTick.lastPrice(), quantity, LocalDateTime.now(tradingClock));
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public BrokerMode mode() {
        return BrokerMode.PAPER;
    }

    private String orderId() {
        return "PAPER-" + UUID.randomUUID();
    }
}
