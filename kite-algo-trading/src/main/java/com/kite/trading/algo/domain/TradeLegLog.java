package com.kite.trading.algo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_leg_log")
@Getter
@Setter
public class TradeLegLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false)
    private TradeLog trade;

    @Enumerated(EnumType.STRING)
    @Column(name = "leg_type", nullable = false)
    private LegType legType;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "charges", precision = 19, scale = 4, nullable = false)
    private BigDecimal charges;

    @Enumerated(EnumType.STRING)
    @Column(name = "broker_mode", nullable = false)
    private BrokerMode brokerMode;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}
