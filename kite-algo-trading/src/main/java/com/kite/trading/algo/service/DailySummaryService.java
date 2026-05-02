package com.kite.trading.algo.service;

import com.kite.trading.algo.domain.DailySummary;
import com.kite.trading.algo.domain.TradeLog;
import com.kite.trading.algo.repository.DailySummaryRepository;
import com.kite.trading.algo.repository.TradeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailySummaryService {

    private final DailySummaryRepository dailySummaryRepository;
    private final TradeLogRepository tradeLogRepository;
    private final Clock tradingClock;
    private final TelegramAlertService telegramAlertService;

    @Transactional
    public DailySummary generateFor(LocalDate tradeDate) {
        LocalDateTime start = tradeDate.atStartOfDay();
        LocalDateTime end = tradeDate.atTime(LocalTime.MAX);
        List<TradeLog> closedTrades = tradeLogRepository.findByExitTimeBetween(start, end);

        DailySummary summary = dailySummaryRepository.findByTradeDate(tradeDate)
                .orElseGet(DailySummary::new);
        summary.setTradeDate(tradeDate);
        summary.setTotalTrades(closedTrades.size());
        summary.setWinningTrades((int) closedTrades.stream().filter(trade -> trade.getNetPnl().compareTo(BigDecimal.ZERO) > 0).count());
        summary.setLosingTrades((int) closedTrades.stream().filter(trade -> trade.getNetPnl().compareTo(BigDecimal.ZERO) < 0).count());
        summary.setGrossPnl(closedTrades.stream().map(TradeLog::getGrossPnl).reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalCharges(closedTrades.stream().map(TradeLog::getCharges).reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setNetPnl(closedTrades.stream().map(TradeLog::getNetPnl).reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setCapitalUsed(closedTrades.stream()
                .map(trade -> trade.getEntryPriceVwap().multiply(BigDecimal.valueOf(trade.getTotalQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setNotes("Generated from " + closedTrades.size() + " closed trades.");
        DailySummary saved = dailySummaryRepository.save(summary);
        telegramAlertService.send("Daily summary " + tradeDate + ": trades=" + saved.getTotalTrades() + ", netPnL=" + saved.getNetPnl());
        return saved;
    }

    public Optional<DailySummary> currentSummary() {
        return dailySummaryRepository.findByTradeDate(LocalDate.now(tradingClock));
    }

    @Scheduled(cron = "${report.summary-cron:0 45 15 * * MON-FRI}", zone = "Asia/Kolkata")
    public void generateToday() {
        DailySummary summary = generateFor(LocalDate.now(tradingClock));
        log.info("Generated daily summary for {} with {} trades", summary.getTradeDate(), summary.getTotalTrades());
    }
}
