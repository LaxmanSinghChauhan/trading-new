package com.kite.trading.algo.web;

import com.kite.trading.algo.service.RecentSignalStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
public class SignalController {

    private final RecentSignalStore recentSignalStore;

    @GetMapping("/recent")
    public ResponseEntity<?> recent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(recentSignalStore.recent(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(recentSignalStore.stats());
    }
}
