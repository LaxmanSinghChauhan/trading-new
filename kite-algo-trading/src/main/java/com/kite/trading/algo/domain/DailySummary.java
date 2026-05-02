package com.kite.trading.algo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_summary")
@Getter
@Setter
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_date", nullable = false, unique = true)
    private LocalDate tradeDate;

    @Column(name = "total_trades", nullable = false)
    private Integer totalTrades;

    @Column(name = "winning_trades", nullable = false)
    private Integer winningTrades;

    @Column(name = "losing_trades", nullable = false)
    private Integer losingTrades;

    @Column(name = "gross_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal grossPnl;

    @Column(name = "total_charges", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalCharges;

    @Column(name = "net_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal netPnl;

    @Column(name = "capital_used", precision = 19, scale = 4, nullable = false)
    private BigDecimal capitalUsed;

    @Column(name = "notes", length = 4000)
    private String notes;
}
