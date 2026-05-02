package com.kite.trading.controller;

import com.kite.trading.model.Order;
import com.kite.trading.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderService.OrderRequest request) {
        try {
            Order order = orderService.placeOrder(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> modifyOrder(@PathVariable String orderId,
                                             @RequestBody OrderService.OrderModification modification) {
        try {
            Order order = orderService.modifyOrder(orderId, modification);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderStatus(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrderStatus(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Order>> getOrdersBySymbol(@PathVariable String symbol) {
        List<Order> orders = orderService.getOrdersBySymbol(symbol);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        List<Order> orders = orderService.getRecentOrders(limit);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/positions")
    public ResponseEntity<List<Map<String, Object>>> getPositions() {
        try {
            List<Map<String, Object>> positions = orderService.getPositions();
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<Map<String, Object>>> getHoldings() {
        try {
            List<Map<String, Object>> holdings = orderService.getHoldings();
            return ResponseEntity.ok(holdings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/real")
    public ResponseEntity<List<Order>> getRealOrders() {
        List<Order> orders = orderService.getRealOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paper")
    public ResponseEntity<List<Order>> getPaperOrders() {
        List<Order> orders = orderService.getPaperOrders();
        return ResponseEntity.ok(orders);
    }
}
