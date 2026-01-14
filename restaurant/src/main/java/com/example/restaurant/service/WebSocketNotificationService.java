package com.example.restaurant.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.dto.websocket.OrderUpdate;
import com.example.dto.websocket.TableStatusUpdate;
import com.example.dto.response.OrderItemResponse;
import com.example.dto.response.TableResponse; // reuse
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.TableEntity;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.TableRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
                            table.getId(), "COMPLETED").orElse(null); // cần thêm method repo này

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
        log.info("Chuẩn bị gửi update với {} bàn (sample first: {})", tableResponses.size(), 
             tableResponses.isEmpty() ? "empty" : tableResponses.get(0));
        try {
            messagingTemplate.convertAndSend("/topic/tables", update);
            log.info("GỬI THÀNH CÔNG realtime table update tới /topic/tables");
        } catch (Exception e) {
            System.err.print("lỗi websocket " + e.getMessage());
            log.error("LỖI GỬI WebSocket table update", e);
        }
    }

    public void sendOrderUpdate(Integer tableId) {
        try {
            Order activeOrder = orderRepository.findFirstByTableIdAndStatusNotOrderByUpdateAtDesc(tableId, "COMPLETED")
                    .orElse(null);

            if (activeOrder != null) {
                OrderUpdate update = mapToOrderUpdate(activeOrder); // bạn tự viết map tương tự trước
                messagingTemplate.convertAndSend("/topic/orders/" + tableId, update);
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAdminDashboardUpdate() {
        try {
            sendTableStatusUpdate();
        } catch (Exception e) {
            System.err.print("Admin update thất bại " + e.getMessage());
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