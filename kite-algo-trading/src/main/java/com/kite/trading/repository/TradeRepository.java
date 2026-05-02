package com.kite.trading.repository;

import com.kite.trading.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByTradeId(String tradeId);

    List<Trade> findBySymbol(String symbol);

    List<Trade> findByStrategyName(String strategyName);

    List<Trade> findByStatus(Trade.TradeStatus status);

    List<Trade> findByStatusAndPaperTradingFalse(Trade.TradeStatus status);

    @Query("SELECT t FROM Trade t WHERE t.entryTime BETWEEN :startDate AND :endDate")
    List<Trade> findByEntryDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM Trade t WHERE t.status = :status AND t.paperTrading = false ORDER BY t.entryTime DESC")
    List<Trade> findOpenTrades(Trade.TradeStatus status);

    List<Trade> findByPaperTradingFalseOrderByEntryTimeDesc();

    List<Trade> findByPaperTradingTrueOrderByEntryTimeDesc();
}
