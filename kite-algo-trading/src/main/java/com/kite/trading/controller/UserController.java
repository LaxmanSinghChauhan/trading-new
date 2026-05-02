package com.kite.trading.controller;

import com.kite.trading.service.KiteAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final KiteAuthService kiteAuthService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get user portfolio and margins
     */
    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio() {
        try {
            if (!kiteAuthService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String apiUrl = "https://api.kite.trade/user/margins";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", kiteAuthService.getAuthorizationHeader());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to fetch portfolio"));
            }
        } catch (Exception e) {
            log.error("Error fetching portfolio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch portfolio: " + e.getMessage()));
        }
    }

    /**
     * Get user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            if (!kiteAuthService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String apiUrl = "https://api.kite.trade/user/profile";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", kiteAuthService.getAuthorizationHeader());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to fetch profile"));
            }
        } catch (Exception e) {
            log.error("Error fetching profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    /**
     * Get user holdings (bought shares)
     */
    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings() {
        try {
            if (!kiteAuthService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String apiUrl = "https://api.kite.trade/portfolio/holdings";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", kiteAuthService.getAuthorizationHeader());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to fetch holdings"));
            }
        } catch (Exception e) {
            log.error("Error fetching holdings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch holdings: " + e.getMessage()));
        }
    }

    /**
     * Get user positions
     */
    @GetMapping("/positions")
    public ResponseEntity<?> getPositions() {
        try {
            if (!kiteAuthService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String apiUrl = "https://api.kite.trade/portfolio/positions";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", kiteAuthService.getAuthorizationHeader());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to fetch positions"));
            }
        } catch (Exception e) {
            log.error("Error fetching positions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch positions: " + e.getMessage()));
        }
    }

    /**
     * Get user orders
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders() {
        try {
            if (!kiteAuthService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String apiUrl = "https://api.kite.trade/orders";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", kiteAuthService.getAuthorizationHeader());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to fetch orders"));
            }
        } catch (Exception e) {
            log.error("Error fetching orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch orders: " + e.getMessage()));
        }
    }
}