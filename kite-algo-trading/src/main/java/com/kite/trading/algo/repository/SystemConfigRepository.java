package com.kite.trading.algo.repository;

import com.kite.trading.algo.domain.SystemConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntry, String> {
}
