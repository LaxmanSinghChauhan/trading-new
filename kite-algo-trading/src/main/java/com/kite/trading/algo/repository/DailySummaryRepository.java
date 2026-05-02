package com.kite.trading.algo.repository;

import com.kite.trading.algo.domain.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    Optional<DailySummary> findByTradeDate(LocalDate tradeDate);
}
