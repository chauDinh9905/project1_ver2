package com.example.restaurant.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Order;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderItemRepository;
import com.example.restaurant.repository.OrderRepository;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    
    public OrderItemService(OrderItemRepository orderItemRepository1, OrderRepository orderRepository1, MenuItemRepository menuItemRepository1){
        this.orderItemRepository = orderItemRepository1;
        this.orderRepository = orderRepository1;
        this.menuItemRepository = menuItemRepository1;
    }

    public OrderItem getOrderItemById(Integer id){
        return orderItemRepository.findById(id).orElseThrow(()->new RuntimeException("id của orderItem ko tồn tại"));
    }
    
    @Transactional
    public List<OrderItem> getAllOrderItemByOrderId(Integer orderId){
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public OrderItem createOrderItem(Integer orderId, Integer menuItemId){
        Order order = orderRepository.findById(orderId).orElseThrow(()->new RuntimeException("id của order không hợp lệ"));
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(()->new RuntimeException("id của menuItem không hợp lệ"));
        OrderItem orderItem = new OrderItem();

        orderItem.setMenuItem(menuItem);
        orderItem.setOrder(order);

        orderItem.setQuantity(1);
        orderItem.setPrice(menuItem.getPrice());

        order.addOrderItem(orderItem);
        recalculateOrderTotal(order);
        return orderItemRepository.save(orderItem);
    }
    @Transactional
    public OrderItem updateOrderItem(Integer orderItemId, OrderItem OI){
        OrderItem oldOI = getOrderItemById(orderItemId);
        if(OI.getNotes() != null){
            oldOI.setNotes(OI.getNotes());
        }
        if(OI.getQuantity() != null){
            oldOI.setQuantity(OI.getQuantity());
        }
            // Lưu thay đổi của item trước
        OrderItem savedItem = orderItemRepository.save(oldOI);
    
            // Tính lại tiền dựa trên Order của item vừa lưu
        recalculateOrderTotal(savedItem.getOrder());
    
        return savedItem;
    }

    @Transactional
    public void deleteOrderItem(Integer orderItemId){
        OrderItem OI = getOrderItemById(orderItemId);
        Order order = OI.getOrder();

        order.getOrderItems().remove(OI);
        orderItemRepository.delete(OI);

        recalculateOrderTotal(order);
    }
    private void recalculateOrderTotal(Order order) {
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);
        orderRepository.save(order);
    }
}
