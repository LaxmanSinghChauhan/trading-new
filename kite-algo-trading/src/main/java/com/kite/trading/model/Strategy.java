package com.kite.trading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "strategies", indexes = {
    @Index(name = "idx_active", columnList = "isActive"),
    @Index(name = "idx_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String strategyId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType type;

    @Lob
    private String parameters;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean paperTrading;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastExecuted;

    private Integer totalTrades;

    private Integer winningTrades;

    private Integer losingTrades;

    @Column(precision = 19, scale = 4)
    private java.math.BigDecimal totalPnl;

    @Column(precision = 5, scale = 2)
    private java.math.BigDecimal winRate;

    public enum StrategyType {
        SUDDEN_MOVEMENT, MOMENTUM, MEAN_REVERSION, BREAKOUT, CUSTOM
    }
}
