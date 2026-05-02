package com.kite.trading.service;

import com.kite.trading.model.Order;
import com.kite.trading.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final KiteConnectService kiteConnectService;
    private final OrderRepository orderRepository;

    @Value("${trading.paper-trading:true}")
    private boolean paperTrading;

    @Value("${trading.default-quantity:1}")
    private int defaultQuantity;

    @Value("${trading.risk.percentage:2.0}")
    private double riskPercentage;

    @Transactional
    public Order placeOrder(OrderRequest orderRequest) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }

        validateOrderRequest(orderRequest);

        Order order = createOrderFromRequest(orderRequest);

        if (paperTrading) {
            order = placePaperOrder(order);
        } else {
            order = placeRealOrder(order);
        }

        orderRepository.save(order);
        log.info("Order placed successfully: {}", order.getOrderId());
        return order;
    }

    @Transactional
    public Order modifyOrder(String orderId, OrderModification modification) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be modified");
        }

        if (paperTrading) {
            order = modifyPaperOrder(order, modification);
        } else {
            order = modifyRealOrder(order, modification);
        }

        orderRepository.save(order);
        log.info("Order modified successfully: {}", orderId);
        return order;
    }

    @Transactional
    public Order cancelOrder(String orderId) throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }

        if (paperTrading) {
            order.setStatus(Order.OrderStatus.CANCELLED);
        } else {
            kiteConnectService.cancelOrder(orderId, "regular");
            order.setStatus(Order.OrderStatus.CANCELLED);
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Order cancelled successfully: {}", orderId);
        return order;
    }

    public Order getOrderStatus(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersBySymbol(String symbol) {
        return orderRepository.findBySymbol(symbol);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPositions() throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }
        return kiteConnectService.getPositions();
    }

    public List<Map<String, Object>> getHoldings() throws Exception {
        if (!kiteConnectService.isInitialized()) {
            throw new IllegalStateException("Kite Connect not initialized");
        }
        return kiteConnectService.getHoldings();
    }

    public List<Order> getRealOrders() {
        return orderRepository.findByPaperTradingFalseOrderByCreatedAtDesc();
    }

    public List<Order> getPaperOrders() {
        return orderRepository.findByPaperTradingTrueOrderByCreatedAtDesc();
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request.getSymbol() == null || request.getSymbol().isEmpty()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        if (request.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.getOrderType() == Order.OrderType.LIMIT && request.getPrice() == null) {
            throw new IllegalArgumentException("Price is required for limit orders");
        }
        if (request.getOrderType() == Order.OrderType.STOP_LOSS_LIMIT && request.getTriggerPrice() == null) {
            throw new IllegalArgumentException("Trigger price is required for stop loss limit orders");
        }
    }

    private Order createOrderFromRequest(OrderRequest request) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setSymbol(request.getSymbol());
        order.setExchange(request.getExchange() != null ? request.getExchange() : "NSE");
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : Order.OrderType.MARKET);
        order.setTransactionType(request.getTransactionType());
        order.setQuantity(request.getQuantity() != null ? request.getQuantity() : defaultQuantity);
        order.setPrice(request.getPrice());
        order.setTriggerPrice(request.getTriggerPrice());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setAveragePrice(BigDecimal.ZERO);
        order.setStopLoss(request.getStopLoss());
        order.setTakeProfit(request.getTakeProfit());
        order.setRemarks(request.getRemarks());
        order.setPaperTrading(paperTrading);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private Order placePaperOrder(Order order) {
        order.setStatus(Order.OrderStatus.COMPLETE);
        order.setAveragePrice(order.getPrice() != null ? order.getPrice() : BigDecimal.ZERO);
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private Order placeRealOrder(Order order) throws Exception {
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("exchange", order.getExchange());
        orderParams.put("tradingsymbol", order.getSymbol());
        orderParams.put("transaction_type", order.getTransactionType().name().toLowerCase());
        orderParams.put("quantity", order.getQuantity());
        orderParams.put("order_type", order.getOrderType().name().toLowerCase());
        orderParams.put("product", "CNC");
        orderParams.put("variety", "regular");

        if (order.getPrice() != null) {
            orderParams.put("price", order.getPrice());
        }
        if (order.getTriggerPrice() != null) {
            orderParams.put("trigger_price", order.getTriggerPrice());
        }

        Map<String, Object> response = kiteConnectService.placeOrder(orderParams, "regular");
        order.setOrderId((String) response.get("order_id"));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private Order modifyPaperOrder(Order order, OrderModification modification) {
        if (modification.getPrice() != null) {
            order.setPrice(modification.getPrice());
        }
        if (modification.getQuantity() != null) {
            order.setQuantity(modification.getQuantity());
        }
        if (modification.getTriggerPrice() != null) {
            order.setTriggerPrice(modification.getTriggerPrice());
        }
        if (modification.getStopLoss() != null) {
            order.setStopLoss(modification.getStopLoss());
        }
        if (modification.getTakeProfit() != null) {
            order.setTakeProfit(modification.getTakeProfit());
        }
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private Order modifyRealOrder(Order order, OrderModification modification) throws Exception {
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("order_id", order.getOrderId());
        orderParams.put("variety", "regular");

        if (modification.getPrice() != null) {
            orderParams.put("price", modification.getPrice());
            order.setPrice(modification.getPrice());
        }
        if (modification.getQuantity() != null) {
            orderParams.put("quantity", modification.getQuantity());
            order.setQuantity(modification.getQuantity());
        }
        if (modification.getTriggerPrice() != null) {
            orderParams.put("trigger_price", modification.getTriggerPrice());
            order.setTriggerPrice(modification.getTriggerPrice());
        }

        kiteConnectService.modifyOrder(orderParams, "regular");
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public static class OrderRequest {
        private String symbol;
        private String exchange;
        private Order.OrderType orderType;
        private Order.TransactionType transactionType;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal triggerPrice;
        private BigDecimal stopLoss;
        private BigDecimal takeProfit;
        private String remarks;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getExchange() { return exchange; }
        public void setExchange(String exchange) { this.exchange = exchange; }
        public Order.OrderType getOrderType() { return orderType; }
        public void setOrderType(Order.OrderType orderType) { this.orderType = orderType; }
        public Order.TransactionType getTransactionType() { return transactionType; }
        public void setTransactionType(Order.TransactionType transactionType) { this.transactionType = transactionType; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getTriggerPrice() { return triggerPrice; }
        public void setTriggerPrice(BigDecimal triggerPrice) { this.triggerPrice = triggerPrice; }
        public BigDecimal getStopLoss() { return stopLoss; }
        public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
        public BigDecimal getTakeProfit() { return takeProfit; }
        public void setTakeProfit(BigDecimal takeProfit) { this.takeProfit = takeProfit; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class OrderModification {
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal triggerPrice;
        private BigDecimal stopLoss;
        private BigDecimal takeProfit;

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getTriggerPrice() { return triggerPrice; }
        public void setTriggerPrice(BigDecimal triggerPrice) { this.triggerPrice = triggerPrice; }
        public BigDecimal getStopLoss() { return stopLoss; }
        public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
        public BigDecimal getTakeProfit() { return takeProfit; }
        public void setTakeProfit(BigDecimal takeProfit) { this.takeProfit = takeProfit; }
    }
}
