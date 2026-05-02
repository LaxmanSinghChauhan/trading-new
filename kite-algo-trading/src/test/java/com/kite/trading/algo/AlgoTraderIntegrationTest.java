package com.kite.trading.algo;

import com.kite.trading.algo.domain.DailySummary;
import com.kite.trading.algo.domain.TradeLifecycleStatus;
import com.kite.trading.algo.domain.TradeLog;
import com.kite.trading.algo.repository.TradeLegLogRepository;
import com.kite.trading.algo.repository.TradeLogRepository;
import com.kite.trading.algo.runtime.Tick;
import com.kite.trading.algo.service.DailySummaryService;
import com.kite.trading.algo.service.PositionManager;
import com.kite.trading.algo.service.StockUniverseService;
import com.kite.trading.algo.service.TickerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AlgoTraderIntegrationTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2026-05-04T04:30:00Z"), IST);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private StockUniverseService stockUniverseService;

    @org.springframework.beans.factory.annotation.Autowired
    private TickerService tickerService;

    @org.springframework.beans.factory.annotation.Autowired
    private TradeLogRepository tradeLogRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private TradeLegLogRepository tradeLegLogRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private PositionManager positionManager;

    @org.springframework.beans.factory.annotation.Autowired
    private DailySummaryService dailySummaryService;

    @BeforeEach
    void setUp() {
        stockUniverseService.refreshUniverse();
    }

    @Test
    void shouldRunOnePaperTradeLifecycleFromSignalToSummary() throws Exception {
        assertThat(stockUniverseService.activeUniverse()).isNotEmpty();

        ingestTick(256265L, "NIFTY_50", "22600.00", 1000, time(0));
        ingestTick(1001L, "RELIANCE", "100.00", 100, time(1));
        ingestTick(1001L, "RELIANCE", "100.05", 100, time(4));
        ingestTick(1001L, "RELIANCE", "100.08", 100, time(7));
        ingestTick(1001L, "RELIANCE", "100.10", 100, time(10));
        ingestTick(1001L, "RELIANCE", "100.12", 100, time(13));
        ingestTick(1001L, "RELIANCE", "100.16", 100, time(16));
        ingestTick(1001L, "RELIANCE", "100.20", 100, time(19));
        ingestTick(1001L, "RELIANCE", "100.25", 100, time(22));
        ingestTick(1001L, "RELIANCE", "100.30", 100, time(25));
        ingestTick(1001L, "RELIANCE", "100.35", 100, time(27));
        ingestTick(1001L, "RELIANCE", "100.60", 1000, time(30));

        TradeLog openTrade = awaitTradeWithStatus(TradeLifecycleStatus.OPEN);
        assertThat(openTrade.getSymbol()).isEqualTo("RELIANCE");
        assertThat(positionManager.openPositionCount()).isEqualTo(1);

        ingestTick(1001L, "RELIANCE", "101.70", 1500, time(35));
        waitForCondition(() -> tradeLegLogRepository.findByTradeIdOrderByExecutedAtAsc(openTrade.getId()).size() >= 2);

        ingestTick(1001L, "RELIANCE", "99.70", 1800, time(40));
        TradeLog closedTrade = awaitTradeWithStatus(TradeLifecycleStatus.CLOSED);

        assertThat(closedTrade.getRemainingQuantity()).isZero();
        assertThat(tradeLegLogRepository.findByTradeIdOrderByExecutedAtAsc(closedTrade.getId())).hasSize(3);
        assertThat(closedTrade.getFinalExitReason()).isEqualTo("STOP_LOSS");
        assertThat(positionManager.openPositionCount()).isZero();

        DailySummary summary = dailySummaryService.generateFor(LocalDate.of(2026, 5, 4));
        assertThat(summary.getTotalTrades()).isEqualTo(1);
        assertThat(summary.getNetPnl()).isEqualTo(closedTrade.getNetPnl());
    }

    private void ingestTick(long token, String symbol, String price, long volume, LocalDateTime tickTime) {
        tickerService.ingest(new Tick(token, symbol, new BigDecimal(price), volume, tickTime));
    }

    private LocalDateTime time(int secondsOffset) {
        return LocalDateTime.of(2026, 5, 4, 10, 0).plusSeconds(secondsOffset);
    }

    private TradeLog awaitTradeWithStatus(TradeLifecycleStatus status) throws Exception {
        waitForCondition(() -> !tradeLogRepository.findByStatus(status).isEmpty());
        List<TradeLog> trades = tradeLogRepository.findByStatus(status);
        return trades.get(0);
    }

    private void waitForCondition(Check check) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000L;
        while (System.currentTimeMillis() < deadline) {
            if (check.evaluate()) {
                return;
            }
            Thread.sleep(100L);
        }
        throw new AssertionError("Condition not met before timeout");
    }

    @FunctionalInterface
    private interface Check {
        boolean evaluate();
    }
}
