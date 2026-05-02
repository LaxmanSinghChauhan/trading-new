package com.kite.trading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = {
    @Index(name = "idx_symbol", columnList = "symbol"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String exchange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    private BigDecimal price;

    private BigDecimal triggerPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal averagePrice;

    private BigDecimal stopLoss;

    private BigDecimal takeProfit;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String remarks;

    @Column(nullable = false)
    private Boolean paperTrading;

    public enum OrderType {
        MARKET, LIMIT, STOP_LOSS_MARKET, STOP_LOSS_LIMIT
    }

    public enum TransactionType {
        BUY, SELL
    }

    public enum OrderStatus {
        PENDING, COMPLETE, REJECTED, CANCELLED, TRIGGER_PENDING
    }
}
