package com.kite.trading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "market_data", indexes = {
    @Index(name = "idx_symbol_timestamp", columnList = "symbol,timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String dataInterval;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal close;

    @Column(nullable = false)
    private Long volume;

    @Column(precision = 19, scale = 4)
    private BigDecimal vwap;
}
