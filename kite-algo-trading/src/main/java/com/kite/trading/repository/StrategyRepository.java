package com.kite.trading.repository;

import com.kite.trading.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    Optional<Strategy> findByStrategyId(String strategyId);

    List<Strategy> findByIsActiveTrue();

    List<Strategy> findByIsActiveFalse();

    List<Strategy> findByType(Strategy.StrategyType type);

    List<Strategy> findByIsActiveTrueAndType(Strategy.StrategyType type);

    List<Strategy> findByPaperTradingFalse();

    List<Strategy> findByPaperTradingTrue();
}
