package com.kite.trading.algo.repository;

import com.kite.trading.algo.domain.TradeLifecycleStatus;
import com.kite.trading.algo.domain.TradeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeLogRepository extends JpaRepository<TradeLog, Long> {

    List<TradeLog> findByStatus(TradeLifecycleStatus status);

    List<TradeLog> findByExitTimeBetween(LocalDateTime start, LocalDateTime end);

    List<TradeLog> findTop50ByOrderByEntryTimeDesc();
}
