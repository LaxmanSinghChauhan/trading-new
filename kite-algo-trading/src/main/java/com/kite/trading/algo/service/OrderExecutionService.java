package com.kite.trading.algo.service;

import com.kite.trading.algo.runtime.BrokerOrderResult;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.runtime.TradeIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionService {

    private final BrokerGatewayResolver brokerGatewayResolver;
    private final TickDataStore tickDataStore;
    private final PositionManager positionManager;
    private final DailyCapitalTracker dailyCapitalTracker;
    private final TelegramAlertService telegramAlertService;

    public void execute(TradeIntent intent) {
        BrokerGateway gateway = brokerGatewayResolver.current();
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                BrokerOrderResult result = gateway.placeBuy(intent);
                positionManager.openPosition(intent, result, gateway.mode());
                log.info("Buy executed for {} at {}", intent.symbol(), result.filledPrice());
                return;
            } catch (Exception exception) {
                log.warn("Buy attempt {} failed for {}", attempts, intent.symbol(), exception);
                if (momentumDecayed(intent)) {
                    dailyCapitalTracker.releaseCapital(intent.reservedCapital());
                    telegramAlertService.send("Abandoned BUY for " + intent.symbol() + " because momentum faded.");
                    return;
                }
                sleep(500L);
            }
        }

        dailyCapitalTracker.releaseCapital(intent.reservedCapital());
        telegramAlertService.send("BUY failed after retries for " + intent.symbol());
    }

    private boolean momentumDecayed(TradeIntent intent) {
        Tick latestTick = tickDataStore.getLatestTick(intent.instrumentToken()).orElse(null);
        if (latestTick == null) {
            return true;
        }
        BigDecimal floorPrice = intent.triggerPrice().multiply(new BigDecimal("0.997"));
        return latestTick.lastPrice().compareTo(floorPrice) < 0;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
