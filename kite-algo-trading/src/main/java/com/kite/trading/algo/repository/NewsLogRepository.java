package com.kite.trading.algo.repository;

import com.kite.trading.algo.domain.NewsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsLogRepository extends JpaRepository<NewsLog, Long> {

    List<NewsLog> findTop50ByOrderByProcessedAtDesc();
}
