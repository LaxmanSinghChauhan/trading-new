package com.kite.trading.algo.web;

import com.kite.trading.algo.service.RiskEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskEngine riskEngine;

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(riskEngine.status());
    }
}
