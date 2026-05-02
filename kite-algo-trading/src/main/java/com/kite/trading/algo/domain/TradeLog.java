package com.kite.trading.algo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_log")
@Getter
@Setter
public class TradeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "instrument_token", nullable = false)
    private Long instrumentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "broker_mode", nullable = false)
    private BrokerMode brokerMode;

    @Column(name = "signal_strength", nullable = false)
    private String signalStrength;

    @Column(name = "entry_price_vwap", precision = 19, scale = 4, nullable = false)
    private BigDecimal entryPriceVwap;

    @Column(name = "exit_price_vwap", precision = 19, scale = 4)
    private BigDecimal exitPriceVwap;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "final_exit_reason")
    private String finalExitReason;

    @Column(name = "gross_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal grossPnl;

    @Column(name = "charges", precision = 19, scale = 4, nullable = false)
    private BigDecimal charges;

    @Column(name = "net_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal netPnl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeLifecycleStatus status;
}
