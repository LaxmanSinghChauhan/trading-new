package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.BrokerMode;
import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.runtime.TradeIntent;
import com.kite.trading.service.KiteConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KiteBrokerGateway implements BrokerGateway {

    private final KiteConnectService kiteConnectService;
    private final TickDataStore tickDataStore;
    private final Clock tradingClock;

    @Override
    public BrokerOrderResult placeBuy(TradeIntent intent) {
        Map<String, Object> params = baseParams(intent.symbol(), intent.quantity(), "buy");
        Map<String, Object> response = kiteConnectService.placeOrder(params, "regular");
        Tick latestTick = tickDataStore.getLatestTick(intent.instrumentToken()).orElseThrow();
        return new BrokerOrderResult(
                String.valueOf(response.get("order_id")),
                latestTick.lastPrice(),
                intent.quantity(),
                LocalDateTime.now(tradingClock)
        );
    }

    @Override
    public BrokerOrderResult placeSell(String symbol, long token, int quantity, String reason) {
        Map<String, Object> params = baseParams(symbol, quantity, "sell");
        Map<String, Object> response = kiteConnectService.placeOrder(params, "regular");
        Tick latestTick = tickDataStore.getLatestTick(token).orElseThrow();
        return new BrokerOrderResult(
                String.valueOf(response.get("order_id")),
                latestTick.lastPrice(),
                quantity,
                LocalDateTime.now(tradingClock)
        );
    }

    @Override
    public boolean isConnected() {
        return kiteConnectService.isInitialized();
    }

    @Override
    public BrokerMode mode() {
        return BrokerMode.LIVE;
    }

    private Map<String, Object> baseParams(String symbol, int quantity, String transactionType) {
        Map<String, Object> params = new HashMap<>();
        params.put("exchange", "NSE");
        params.put("tradingsymbol", symbol);
        params.put("transaction_type", transactionType);
        params.put("quantity", quantity);
        params.put("order_type", "market");
        params.put("product", "MIS");
        params.put("variety", "regular");
        params.putIfAbsent("price", BigDecimal.ZERO);
        return params;
    }
}
