package com.example.restaurant.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.request.CreateOrderItemRequest;
import com.example.dto.request.CreateOrderRequest;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.TableRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final WebSocketNotificationService wsNotificationService;

    public OrderService(OrderRepository orderRepository1, TableRepository tableRepository1, MenuItemRepository menuItemRepository1, WebSocketNotificationService wsNotificationService1){
        this.orderRepository = orderRepository1;
        this.tableRepository = tableRepository1;
        this.menuItemRepository = menuItemRepository1;
        this.wsNotificationService = wsNotificationService1;
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
        Order savedOrder = orderRepository.save(newOrder);
        wsNotificationService.sendOrderUpdate(tableId);
        wsNotificationService.sendTableStatusUpdate();
        wsNotificationService.sendAdminDashboardUpdate();
        return savedOrder;
    }

    @Transactional
    public Order updateOrder(Integer id, Order O){
        Order oldO = getOrderById(id);
        Integer tableId = oldO.getTable().getId();
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
        Order newOrder = orderRepository.save(oldO);
        wsNotificationService.sendOrderUpdate(tableId);
        wsNotificationService.sendAdminDashboardUpdate();
        return newOrder;
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
        wsNotificationService.sendOrderUpdate(tableId);
        wsNotificationService.sendTableStatusUpdate();
        wsNotificationService.sendAdminDashboardUpdate();
    }

    @Transactional
    public Order createOrder(Integer tableId, List<OrderItem> items, String notes) { 
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));

        // Optional: check nếu bàn đang AVAILABLE mới cho tạo (tránh tạo nhiều order active cùng lúc)
        if ("OCCUPIED".equals(table.getStatus())) {
            int activeCount = orderRepository.countByTableIdAndStatusNot(tableId, "COMPLETED");
            if (activeCount > 0) {
                throw new RuntimeException("Bàn đang có đơn hàng đang xử lý");
            }
        }

        Order newOrder = new Order();
        newOrder.setTable(table);
        newOrder.setStatus("PENDING"); // default
        newOrder.setNotes(notes);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(newOrder);
            newOrder.getOrderItems().add(item);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        newOrder.setTotalPrice(total);
        Order savedOrder = orderRepository.save(newOrder);

        if ("AVAILABLE".equals(table.getStatus())) {
            table.setStatus("OCCUPIED");
            tableRepository.save(table);
        }
        wsNotificationService.sendOrderUpdate(tableId);
        wsNotificationService.sendTableStatusUpdate();
        wsNotificationService.sendAdminDashboardUpdate();
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        
        wsNotificationService.sendOrderUpdate(order.getTable().getId());
        wsNotificationService.sendAdminDashboardUpdate();
        
        return updated;
    }

    @Transactional
    public Order createOrderFromRequest(CreateOrderRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Món ăn ID " + itemReq.getMenuItemId() + " không tồn tại"));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(menuItem.getPrice()); // Lấy giá từ menu
            orderItem.setNotes(itemReq.getNotes());
            
            orderItems.add(orderItem);
        }
        
        // Gọi method createOrder đã có
        return createOrder(request.getTableId(), orderItems, request.getNotes());
    }
}
