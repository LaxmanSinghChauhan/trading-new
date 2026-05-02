package com.kite.trading.repository;

import com.kite.trading.model.PriceMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceMovementRepository extends JpaRepository<PriceMovement, Long> {

    List<PriceMovement> findBySymbol(String symbol);

    List<PriceMovement> findBySymbolOrderByTimestampDesc(String symbol);

    List<PriceMovement> findByMovementType(PriceMovement.MovementType movementType);

    List<PriceMovement> findBySignalGeneratedTrue();

    @Query("SELECT p FROM PriceMovement p WHERE p.symbol = :symbol AND p.timestamp BETWEEN :startDate AND :endDate ORDER BY p.timestamp DESC")
    List<PriceMovement> findBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM PriceMovement p WHERE p.movementType = :movementType AND p.signalGenerated = true ORDER BY p.timestamp DESC")
    List<PriceMovement> findRecentSignals(PriceMovement.MovementType movementType);

    @Query("SELECT p FROM PriceMovement p WHERE p.timestamp >= :startDate ORDER BY p.timestamp DESC")
    List<PriceMovement> findRecentMovements(LocalDateTime startDate);
}
