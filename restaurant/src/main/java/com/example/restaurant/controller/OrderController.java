package com.example.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Integer id){
        return orderService.getOrderById(id);
    }

    @GetMapping("/all/{tableId}")
    public List<Order> getOrderByTableId(@PathVariable Integer tableId){
        return orderService.getOrderByTableId(tableId);
    }

    @GetMapping("/all/{status}")
    public List<Order> getOrderByStatus(@PathVariable String status){
        return orderService.getOrderByStatus(status);
    }

    @PostMapping("/{tableId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order creatOrder(@PathVariable Integer tableId, @RequestBody List<OrderItem> O){
        return orderService.createOrder(tableId, O);
    }
    
    @PutMapping("/{id}")
    public Order updatOrder(@PathVariable Integer id, @RequestBody Order O){
        return orderService.updateOrder(id, O);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id){
        orderService.deleteOrder(id);
    }
}
