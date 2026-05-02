package com.kite.trading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trades", indexes = {
    @Index(name = "idx_symbol", columnList = "symbol"),
    @Index(name = "idx_entry_time", columnList = "entryTime"),
    @Index(name = "idx_exit_time", columnList = "exitTime")
})
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tradeId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String strategyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType tradeType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal entryPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal exitPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal stopLoss;

    @Column(precision = 19, scale = 4)
    private BigDecimal takeProfit;

    @Column(precision = 19, scale = 4)
    private BigDecimal pnl;

    @Column(precision = 5, scale = 2)
    private BigDecimal pnlPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private String entryOrderId;

    private String exitOrderId;

    @Column(nullable = false)
    private Boolean paperTrading;

    private String remarks;

    public enum TradeType {
        LONG, SHORT
    }

    public enum TradeStatus {
        OPEN, CLOSED, STOPPED_OUT, PROFIT_TAKEN
    }
}
