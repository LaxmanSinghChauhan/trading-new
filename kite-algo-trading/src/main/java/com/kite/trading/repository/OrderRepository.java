package com.kite.trading.repository;

import com.kite.trading.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    List<Order> findBySymbol(String symbol);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findBySymbolAndStatus(String symbol, Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByPaperTradingFalseOrderByCreatedAtDesc();

    List<Order> findByPaperTradingTrueOrderByCreatedAtDesc();
}
