package com.kite.trading.repository;

import com.kite.trading.model.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    List<MarketData> findBySymbol(String symbol);

    List<MarketData> findBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol AND m.timestamp BETWEEN :startDate AND :endDate ORDER BY m.timestamp ASC")
    List<MarketData> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol AND m.dataInterval = :interval ORDER BY m.timestamp DESC")
    List<MarketData> findBySymbolAndInterval(String symbol, String interval);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol AND m.timestamp >= :startDate ORDER BY m.timestamp ASC")
    List<MarketData> findLatestBySymbol(String symbol, LocalDateTime startDate);

    @Query("SELECT m FROM MarketData m WHERE m.symbol = :symbol ORDER BY m.timestamp DESC LIMIT 1")
    MarketData findLatestBySymbol(String symbol);
}
