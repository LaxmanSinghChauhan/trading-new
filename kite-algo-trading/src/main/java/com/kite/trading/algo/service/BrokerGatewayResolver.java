package com.kite.trading.algo.service;

import com.kite.trading.algo.config.BrokerProperties;
import com.kite.trading.algo.domain.BrokerMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrokerGatewayResolver {

    private final BrokerProperties brokerProperties;
    private final PaperBrokerGateway paperBrokerGateway;
    private final KiteBrokerGateway kiteBrokerGateway;

    public BrokerGateway current() {
        return mode() == BrokerMode.LIVE ? kiteBrokerGateway : paperBrokerGateway;
    }

    public BrokerMode mode() {
        try {
            return BrokerMode.valueOf(brokerProperties.getMode().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return BrokerMode.PAPER;
        }
    }
}
