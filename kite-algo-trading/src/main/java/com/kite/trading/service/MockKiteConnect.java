package com.kite.trading.service;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MockKiteConnect {

    private String apiKey;
    private String apiSecret;
    private String accessToken;
    private String userId;

    public MockKiteConnect(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String getLoginUrl() {
        return "https://kite.trade/connect/login?v=3&api_key=" + apiKey;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public MockQuote getSingleQuote(String instrumentToken) {
        MockQuote quote = new MockQuote();
        quote.setLastPrice(new BigDecimal("1000.00"));
        quote.setClosePrice(new BigDecimal("990.00"));
        quote.setOhlc(new MockOHLC(new BigDecimal("995.00"), new BigDecimal("1005.00"),
                new BigDecimal("990.00"), new BigDecimal("1000.00")));
        quote.setTotalBuyQuantity(1000L);
        quote.setTotalSellQuantity(800L);
        quote.setAverageTradePrice(new BigDecimal("998.00"));
        return quote;
    }

    public java.util.Map<String, MockQuote> getQuote(String instrumentTokens) {
        java.util.Map<String, MockQuote> quotes = new java.util.HashMap<>();
        String[] tokens = instrumentTokens.split(",");
        for (String token : tokens) {
            quotes.put(token, getSingleQuote(token));
        }
        return quotes;
    }

    public java.util.List<MockHistoricalData> getHistoricalData(String instrumentToken, Date fromDate, Date toDate, String interval) {
        java.util.List<MockHistoricalData> data = new java.util.ArrayList<>();
        long currentTime = fromDate.getTime();
        long endTime = toDate.getTime();
        long intervalMillis = getIntervalMillis(interval);

        while (currentTime <= endTime) {
            MockHistoricalData candle = new MockHistoricalData();
            candle.setTimeStamp(new Date(currentTime));
            candle.setOpen("995.00");
            candle.setHigh("1005.00");
            candle.setLow("990.00");
            candle.setClose("1000.00");
            candle.setVolume(1000L);
            data.add(candle);
            currentTime += intervalMillis;
        }
        return data;
    }

    private long getIntervalMillis(String interval) {
        switch (interval) {
            case "minute": return 60 * 1000;
            case "5minute": return 5 * 60 * 1000;
            case "15minute": return 15 * 60 * 1000;
            case "day": return 24 * 60 * 60 * 1000;
            default: return 60 * 1000;
        }
    }

    public java.util.Map<String, Object> generateSession(String requestToken, String apiSecret) {
        java.util.Map<String, Object> session = new java.util.HashMap<>();
        session.put("access_token", "mock_access_token_" + System.currentTimeMillis());
        session.put("user_id", userId);
        return session;
    }

    public java.util.Map<String, Object> placeOrder(java.util.Map<String, Object> orderParams, String variety) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("order_id", "MOCK_ORDER_" + System.currentTimeMillis());
        response.put("status", "success");
        return response;
    }

    public java.util.Map<String, Object> modifyOrder(java.util.Map<String, Object> orderParams, String variety) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("order_id", orderParams.get("order_id"));
        response.put("status", "success");
        return response;
    }

    public java.util.Map<String, Object> cancelOrder(String orderId, String variety) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("order_id", orderId);
        response.put("status", "cancelled");
        return response;
    }

    public java.util.List<java.util.Map<String, Object>> getOrders() {
        return new java.util.ArrayList<>();
    }

    public java.util.List<java.util.Map<String, Object>> getPositions() {
        return new java.util.ArrayList<>();
    }

    public java.util.List<java.util.Map<String, Object>> getHoldings() {
        return new java.util.ArrayList<>();
    }

    public java.util.Map<String, Object> getOrderHistory(String orderId) {
        java.util.Map<String, Object> history = new java.util.HashMap<>();
        history.put("order_id", orderId);
        history.put("status", "complete");
        return history;
    }

    @Data
    public static class MockQuote {
        private BigDecimal lastPrice;
        private BigDecimal closePrice;
        private MockOHLC ohlc;
        private Long totalBuyQuantity;
        private Long totalSellQuantity;
        private BigDecimal averageTradePrice;
    }

    @Data
    public static class MockOHLC {
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;

        public MockOHLC(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }

    @Data
    public static class MockHistoricalData {
        private Date timeStamp;
        private String open;
        private String high;
        private String low;
        private String close;
        private Long volume;
    }
}
