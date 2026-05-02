package com.kite.trading.algo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Getter
@Setter
public class SystemConfigEntry {

    @Id
    @Column(name = "config_key", nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 200)
    private String value;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
