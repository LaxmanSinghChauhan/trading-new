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
import java.time.LocalDateTime;

@Entity
@Table(name = "news_log")
@Getter
@Setter
public class NewsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "headline", nullable = false, length = 2000)
    private String headline;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "sentiment", nullable = false)
    private String sentiment;

    @Column(name = "confidence", precision = 5, scale = 4, nullable = false)
    private BigDecimal confidence;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
