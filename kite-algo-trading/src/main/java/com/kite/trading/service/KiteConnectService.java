package com.kite.trading.service;

import com.kite.trading.config.KiteConnectConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiteConnectService {

    private final KiteConnectConfig config;
    private MockKiteConnect kiteConnect;

    @PostConstruct
    public void init() {
        kiteConnect = config.mockKiteConnect();
        log.info("Kite Connect initialized");
    }

    public void initialize(String accessToken) {
        kiteConnect = new MockKiteConnect(config.getApiKey(), config.getApiSecret());
        kiteConnect.setAccessToken(accessToken);
        kiteConnect.setUserId(config.getApiKey());
        log.info("Kite Connect initialized with access token");
    }

    public String getLoginUrl() {
        return kiteConnect.getLoginUrl();
    }

    public String generateSession(String requestToken) {
        Map<String, Object> sessionData = kiteConnect.generateSession(requestToken, config.getApiSecret());
        String accessToken = (String) sessionData.get("access_token");
        initialize(accessToken);
        return accessToken;
    }

    public MockKiteConnect.MockQuote getQuote(String instrumentToken) {
        return kiteConnect.getSingleQuote(instrumentToken);
    }

    public Map<String, MockKiteConnect.MockQuote> getQuotes(List<String> instrumentTokens) {
        return kiteConnect.getQuote(String.join(",", instrumentTokens));
    }

    public List<MockKiteConnect.MockHistoricalData> getHistoricalData(String instrumentToken, Date fromDate, Date toDate, String interval) {
        return kiteConnect.getHistoricalData(instrumentToken, fromDate, toDate, interval);
    }

    public Map<String, Object> placeOrder(Map<String, Object> orderParams, String variety) {
        return kiteConnect.placeOrder(orderParams, variety);
    }

    public Map<String, Object> modifyOrder(Map<String, Object> orderParams, String variety) {
        return kiteConnect.modifyOrder(orderParams, variety);
    }

    public Map<String, Object> cancelOrder(String orderId, String variety) {
        return kiteConnect.cancelOrder(orderId, variety);
    }

    public List<Map<String, Object>> getOrders() {
        return kiteConnect.getOrders();
    }

    public List<Map<String, Object>> getPositions() {
        return kiteConnect.getPositions();
    }

    public List<Map<String, Object>> getHoldings() {
        return kiteConnect.getHoldings();
    }

    public Map<String, Object> getOrderHistory(String orderId) {
        return kiteConnect.getOrderHistory(orderId);
    }

    public boolean isInitialized() {
        return kiteConnect != null && kiteConnect.getAccessToken() != null;
    }
}
