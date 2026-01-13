package com.example.restaurant.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.request.CreateOrderRequest;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Order> createOrder(@RequestParam Integer tableId, @RequestBody CreateOrderRequest request) {
            Order order = orderService.createOrderFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<Order> getCurrentOrderByTable(@PathVariable Integer tableId) {
        List<Order> orders = orderService.getOrderByTableId(tableId);
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        // Trả về order mới nhất chưa COMPLETED
        Order currentOrder = orders.stream()
            .filter(o -> !"COMPLETED".equals(o.getStatus()))
            .findFirst()
            .orElse(null);
        
        if (currentOrder == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(currentOrder);
    }

    @GetMapping("/table/{tableId}/all")
    public ResponseEntity<List<Order>> getAllOrdersByTable(@PathVariable Integer tableId) {
        List<Order> orders = orderService.getOrderByTableId(tableId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Integer id){
        return orderService.getOrderById(id);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrderByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request
    ) {
        String newStatus = request.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/all/{tableId}")
    public List<Order> getOrderByTableId(@PathVariable Integer tableId){
        return orderService.getOrderByTableId(tableId);
    }

    @GetMapping("/all/{status}")
    public List<Order> getOrderByStatus(@PathVariable String status){
        return orderService.getOrderByStatus(status);
    }

    @PostMapping("/table/{tableId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order creatOrder(@PathVariable Integer tableId, @RequestBody List<OrderItem> O){
        return orderService.createOrder(tableId, O);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Integer id, @RequestBody Order orderUpdate) {
        Order updated = orderService.updateOrder(id, orderUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id){
        orderService.deleteOrder(id);
    }
}
