package com.kite.trading.algo.repository;

import com.kite.trading.algo.domain.TradeLegLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeLegLogRepository extends JpaRepository<TradeLegLog, Long> {

    List<TradeLegLog> findByTradeIdOrderByExecutedAtAsc(Long tradeId);
}
