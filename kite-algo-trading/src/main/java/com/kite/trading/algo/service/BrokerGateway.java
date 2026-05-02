package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.BrokerMode;
import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.TradeIntent;

public interface BrokerGateway {

    BrokerOrderResult placeBuy(TradeIntent intent);

    BrokerOrderResult placeSell(String symbol, long token, int quantity, String reason);

    boolean isConnected();

    BrokerMode mode();
}
