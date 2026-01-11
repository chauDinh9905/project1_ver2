package com.example.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.service.OrderItemService;

@RestController
@RequestMapping("/api/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;
    public OrderItemController(OrderItemService orderItemService){
        this.orderItemService = orderItemService;
    }

    @GetMapping("/{id}")
    public OrderItem getOrderItemById(@PathVariable Integer id){
        return orderItemService.getOrderItemById(id);
    }

    @GetMapping("/all/{orderId}")
    public List<OrderItem> getAllOrderItemByOrderId(@PathVariable Integer orderId){
        return orderItemService.getAllOrderItemByOrderId(orderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItem createOrderItem(@RequestParam Integer orderId, @RequestParam Integer menuItemId){
        return orderItemService.createOrderItem(orderId, menuItemId);
    }  
    
    @PutMapping("/{id}")
    public OrderItem updateOrderItem(@PathVariable Integer id, @RequestBody OrderItem OI){
        return orderItemService.updateOrderItem(id, OI);
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Integer id){
        orderItemService.deleteOrderItem(id);
    }
}
