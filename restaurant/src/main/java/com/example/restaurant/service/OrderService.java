package com.example.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restaurant.entity.Order;
import com.example.restaurant.repository.OrderRepository;
//import com.example.restaurant.repository.TableRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    //private final TableRepository tableRepository;

    public OrderService(OrderRepository orderRepository1){
        this.orderRepository = orderRepository1;
        //this.tableRepository = tableRepository1;
    }

    public Order getOrderById(Integer id){
        return orderRepository.findById(id).orElseThrow(()-> new RuntimeException("id của đơn hàng không hợp lệ"));
    }

    public List<Order> getOrderByTableId(Integer tableId){
        return orderRepository.findByTableIdOrderByCreateAtDesc(tableId);
    }

    public List<Order> getOrderByStatus(String status){
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order updateOrder(Integer id, Order O){
        Order oldO = getOrderById(id);
        if(O.getStatus() != null){
            oldO.setStatus(O.getStatus());
        }
        if(O.getNotes() != null){
            oldO.setNotes(O.getNotes());
        }
        return orderRepository.save(oldO);
    }
}
