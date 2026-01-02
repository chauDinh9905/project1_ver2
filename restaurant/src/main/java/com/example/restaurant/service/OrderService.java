package com.example.restaurant.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.TableRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;

    public OrderService(OrderRepository orderRepository1, TableRepository tableRepository1){
        this.orderRepository = orderRepository1;
        this.tableRepository = tableRepository1;
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
    public Order createOrder(Integer tableId, List<OrderItem> items){
        TableEntity tableEntity = tableRepository.findById(tableId).orElseThrow(()-> new RuntimeException("id bàn không hợp lệ"));

        Order newOrder = new Order();
        newOrder.setTable(tableEntity);

        BigDecimal totalprice = BigDecimal.ZERO;
        for(OrderItem item: items){
            item.setOrder(newOrder);
            newOrder.getOrderItems().add(item);

            BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalprice = totalprice.add(itemPrice);
        }
        newOrder.setTotalPrice(totalprice);
        return orderRepository.save(newOrder);
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
        if(O.getOrderItems() != null){
            oldO.getOrderItems().clear();
            BigDecimal newTotal = BigDecimal.ZERO;
            for(OrderItem item: O.getOrderItems()){
                item.setOrder(oldO);
                oldO.getOrderItems().add(item);
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                newTotal = newTotal.add(itemPrice);
            }
            //oldO.setOrderItems(O.getOrderItems());
            oldO.setTotalPrice(newTotal);
        }
        return orderRepository.save(oldO);
    }

    @Transactional
    public void deleteOrder(Integer id){
        Order order = getOrderById(id);
        TableEntity table = order.getTable();
        Integer tableId = table.getId();
        orderRepository.delete(order);
        Integer orderCount = orderRepository.countByTableId(tableId);
        if(orderCount == 0){
            table.setStatus("AVAILABLE");
            tableRepository.save(table);
        }
    }
}
