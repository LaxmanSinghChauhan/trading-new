package com.kite.trading.service;

import com.kite.trading.config.KiteConnectConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiteAuthService {

    private final KiteConnectConfig kiteConnectConfig;
    private String accessToken;
    private String requestToken;
    private String userId;
    private String userName;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generate the Kite Connect login URL
     */
    public String getLoginUrl() {
        String apiKey = kiteConnectConfig.getApiKey();
        // Kite Connect login URL format
        return String.format("https://kite.zerodha.com/connect/login?v=3&api_key=%s", apiKey);
    }

    /**
     * Process the request token received from Kite Connect callback
     * and exchange it for an access token
     */
    public void processRequestToken(String requestToken) {
        this.requestToken = requestToken;
        log.info("Received request token: {}", requestToken);

        try {
            // Exchange request token for access token
            exchangeToken();
        } catch (Exception e) {
            log.error("Error exchanging request token for access token", e);
            throw new RuntimeException("Failed to exchange request token", e);
        }
    }

    /**
     * Exchange request token for access token with Kite Connect API
     */
    private void exchangeToken() {
        String apiKey = kiteConnectConfig.getApiKey();
        String apiSecret = kiteConnectConfig.getApiSecret();
        String apiUrl = kiteConnectConfig.getApiUrl();

        // Calculate checksum: SHA-256(api_key + request_token + api_secret)
        String checksum = calculateChecksum(apiKey, requestToken, apiSecret);
        log.info("Generated checksum for token exchange");

        // Prepare request body
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("api_key", apiKey);
        requestBody.add("request_token", requestToken);
        requestBody.add("checksum", checksum);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Kite-Version", "3");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Make POST request to token endpoint
            String tokenUrl = apiUrl + "/session/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                if (data != null) {
                    this.accessToken = (String) data.get("access_token");
                    this.userId = (String) data.get("user_id");
                    this.userName = (String) data.get("user_name");

                    log.info("Successfully obtained access token for user: {} ({})", userName, userId);
                    log.info("Access token: {}", accessToken);
                } else {
                    throw new RuntimeException("Invalid response format from Kite Connect");
                }
            } else {
                throw new RuntimeException("Failed to obtain access token. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during token exchange", e);
            throw new RuntimeException("Token exchange failed", e);
        }
    }

    /**
     * Calculate SHA-256 checksum for token exchange
     */
    private String calculateChecksum(String apiKey, String requestToken, String apiSecret) {
        try {
            String data = apiKey + requestToken + apiSecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    /**
     * Get authorization header value for API requests
     */
    public String getAuthorizationHeader() {
        if (accessToken == null) {
            throw new IllegalStateException("Not authenticated. Please login first.");
        }
        return "token " + kiteConnectConfig.getApiKey() + ":" + accessToken;
    }

    /**
     * Process postback notifications from Kite Connect
     */
    public void processPostback(Map<String, Object> payload) {
        log.info("Processing postback: {}", payload);

        // Handle different types of postbacks
        String messageType = (String) payload.get("message_type");

        switch (messageType) {
            case "order_update":
                handleOrderUpdate(payload);
                break;
            case "quote":
                handleQuoteUpdate(payload);
                break;
            default:
                log.warn("Unknown postback message type: {}", messageType);
        }
    }

    /**
     * Handle order update notifications
     */
    private void handleOrderUpdate(Map<String, Object> payload) {
        log.info("Order update received: {}", payload);
        // TODO: Update order status in database
    }

    /**
     * Handle quote update notifications
     */
    private void handleQuoteUpdate(Map<String, Object> payload) {
        log.info("Quote update received: {}", payload);
        // TODO: Update market data in database
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Get the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get the request token
     */
    public String getRequestToken() {
        return requestToken;
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Logout and invalidate session
     */
    public void logout() {
        if (accessToken != null) {
            try {
                String apiKey = kiteConnectConfig.getApiKey();
                String apiUrl = kiteConnectConfig.getApiUrl();
                String logoutUrl = apiUrl + "/session/token?api_key=" + apiKey + "&access_token=" + accessToken;

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Kite-Version", "3");

                HttpEntity<String> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                    logoutUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("Successfully logged out from Kite Connect");
                } else {
                    log.warn("Logout request returned status: {}", response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error during logout", e);
            }
        }

        log.info("Logging out user");
        this.requestToken = null;
        this.accessToken = null;
        this.userId = null;
        this.userName = null;
    }
}