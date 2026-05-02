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
@Table(name = "price_movements", indexes = {
    @Index(name = "idx_symbol_timestamp", columnList = "symbol,timestamp"),
    @Index(name = "idx_movement_type", columnList = "movementType")
})
public class PriceMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal startPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal endPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentageChange;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false)
    private Integer timeframeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private Boolean signalGenerated;

    @Column(precision = 19, scale = 4)
    private BigDecimal rsi;

    @Column(precision = 19, scale = 4)
    private BigDecimal macd;

    @Column(precision = 19, scale = 4)
    private BigDecimal signalLine;

    public enum MovementType {
        SUDDEN_RISE, SUDDEN_FALL, NORMAL
    }
}
