package com.kite.trading.controller;

import com.kite.trading.config.KiteConnectConfig;
import com.kite.trading.service.KiteAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final KiteAuthService kiteAuthService;
    private final KiteConnectConfig kiteConnectConfig;

    /**
     * Get the Kite Connect login URL
     * This endpoint returns the URL where users should be redirected for authentication
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        try {
            String loginUrl = kiteAuthService.getLoginUrl();
            String apiKey = kiteConnectConfig.getApiKey();
            String callbackUrl = kiteConnectConfig.getFullCallbackUrl();

            // Handle null values gracefully
            Map<String, String> response = new java.util.HashMap<>();
            response.put("login_url", loginUrl);
            if (apiKey != null) {
                response.put("api_key", apiKey);
            }
            if (callbackUrl != null) {
                response.put("callback_url", callbackUrl);
            } else {
                response.put("callback_url", "https://haunt-resonant-absence.ngrok-free.dev/callback");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating login URL", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kite Connect callback endpoint
     * This is where Kite redirects after successful authentication
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(@RequestParam("request_token") String requestToken) {
        try {
            log.info("Received callback with request token");
            kiteAuthService.processRequestToken(requestToken);

            // Redirect to dashboard on success
            return new RedirectView("/index.html?auth=success");
        } catch (Exception e) {
            log.error("Error processing callback", e);
            return new RedirectView("/index.html?auth=error&message=" + e.getMessage());
        }
    }

    /**
     * Kite Connect postback endpoint
     * This is where Kite sends order updates and other notifications
     */
    @PostMapping("/postback")
    public ResponseEntity<String> handlePostback(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received postback: {}", payload);
            kiteAuthService.processPostback(payload);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("Error processing postback", e);
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }

    /**
     * Check authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        try {
            boolean isAuthenticated = kiteAuthService.isAuthenticated();

            // Handle null values gracefully
            Map<String, Object> status = new java.util.HashMap<>();
            status.put("authenticated", isAuthenticated);

            String apiKey = kiteConnectConfig.getApiKey();
            if (apiKey != null) {
                status.put("api_key", apiKey);
            }

            String callbackUrl = kiteConnectConfig.getFullCallbackUrl();
            if (callbackUrl != null) {
                status.put("callback_url", callbackUrl);
            } else {
                status.put("callback_url", "https://haunt-resonant-absence.ngrok-free.dev/callback");
            }

            String postbackUrl = kiteConnectConfig.getFullPostbackUrl();
            if (postbackUrl != null) {
                status.put("postback_url", postbackUrl);
            } else {
                status.put("postback_url", "https://haunt-resonant-absence.ngrok-free.dev/api/auth/postback");
            }

            if (isAuthenticated) {
                String userId = kiteAuthService.getUserId();
                String userName = kiteAuthService.getUserName();
                if (userId != null) {
                    status.put("user_id", userId);
                }
                if (userName != null) {
                    status.put("user_name", userName);
                }
            }

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error checking auth status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Logout and clear session
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            kiteAuthService.logout();
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}