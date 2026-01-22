package com.example.restaurant.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.dto.websocket.OrderUpdate;
import com.example.dto.websocket.TableStatusUpdate;
import com.example.dto.response.OrderItemResponse;
import com.example.dto.response.TableResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.TableRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;
    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate,
            TableRepository tableRepository,
            OrderRepository orderRepository) {
        this.messagingTemplate = messagingTemplate;
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    public void sendTableStatusUpdate() {
        List<TableEntity> allTables = tableRepository.findAll();

        List<TableResponse> tableResponses = allTables.stream()
                .map(table -> {
                    Order activeOrder = orderRepository.findFirstByTableIdAndStatusNotOrderByUpdateAtDesc(
                            table.getId(), "COMPLETED").orElse(null);

                    return TableResponse.builder()
                            .id(table.getId())
                            .capacity(table.getCapacity())
                            .status(table.getStatus())
                            .currentOrderId(activeOrder != null ? activeOrder.getId() : null)
                            .totalPrice(activeOrder != null ? activeOrder.getTotalPrice() : BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());

        TableStatusUpdate update = new TableStatusUpdate(tableResponses);
        log.info("📊 Gửi update {} bàn tới /topic/tables", tableResponses.size());
        
        try {
            messagingTemplate.convertAndSend("/topic/tables", update);
            log.info("✅ GỬI THÀNH CÔNG table status update");
        } catch (Exception e) {
            log.error("❌ LỖI GỬI WebSocket table update", e);
        }
    }

    /**
     * ⭐ GỬI ORDER UPDATE CHO 1 BÀN CỤ THỂ
     * Frontend expect:
     * - Nếu có order: gửi OrderUpdate object
     * - Nếu không có order: gửi {status: "NO_ACTIVE_ORDER"}
     */
    public void sendOrderUpdate(Integer tableId) {
    try {
        Order activeOrder = orderRepository.findFirstByTableIdAndStatusNotOrderByUpdateAtDesc(
                tableId, "COMPLETED").orElse(null);

        if (activeOrder != null) {
            OrderUpdate update = mapToOrderUpdate(activeOrder);
            messagingTemplate.convertAndSend("/topic/orders/" + tableId, update);
            log.info("✅ Gửi order #{} cho bàn {} tới /topic/orders/{}", 
                activeOrder.getId(), tableId, tableId);
            
            messagingTemplate.convertAndSend("/topic/admin/orders", update);
            log.info("✅ Gửi order #{} tới admin dashboard", activeOrder.getId());
            
        } else {
            // ⭐ CAST SANG (Object) để tránh ambiguous
            Map<String, Object> noOrderMessage = new HashMap<>();
            noOrderMessage.put("status", "NO_ACTIVE_ORDER");
            noOrderMessage.put("tableId", tableId);
            
            messagingTemplate.convertAndSend("/topic/orders/" + tableId, (Object) noOrderMessage);
            log.info("✅ Gửi NO_ACTIVE_ORDER cho bàn {} tới /topic/orders/{}", tableId, tableId);
        }
    } catch (Exception e) {
        log.error("❌ LỖI gửi order update cho bàn {}", tableId, e);
    }
}

    /**
     * ⭐ GỬI UPDATE CHO ADMIN DASHBOARD
     */
    public void sendAdminDashboardUpdate() {
        try {
            sendTableStatusUpdate();
            log.info("✅ Admin dashboard đã nhận update");
        } catch (Exception e) {
            log.error("❌ Admin update thất bại", e);
        }
    }

    /**
     * ⭐ GỬI 1 ORDER CỤ THỂ CHO ADMIN (dùng khi tạo order mới)
     */
    public void sendOrderToAdmin(Order order) {
    try {
        OrderUpdate update = mapToOrderUpdate(order);
        
        System.out.println("=== SENDING TO ADMIN ===");
        System.out.println("Topic: /topic/admin/orders");
        System.out.println("Order ID: " + update.getOrderId());
        System.out.println("Table ID: " + update.getTableId());
        System.out.println("========================");
        
        messagingTemplate.convertAndSend("/topic/admin/orders", update);
        log.info("✅ Gửi order #{} tới admin qua /topic/admin/orders", order.getId());
    } catch (Exception e) {
        log.error("❌ LỖI gửi order {} tới admin", order.getId(), e);
        e.printStackTrace(); // ← Thêm dòng này để xem stack trace
    }
}

    @Transactional
    private OrderUpdate mapToOrderUpdate(Order order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .name(item.getMenuItem().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .notes(item.getNotes())
                        .build())
                .collect(Collectors.toList());

        return OrderUpdate.builder()
                .orderId(order.getId())
                .tableId(order.getTable().getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .notes(order.getNotes())
                .createAt(order.getCreateAt())
                .updateAt(order.getUpdateAt())
                .items(items)
                .build();
    }
}