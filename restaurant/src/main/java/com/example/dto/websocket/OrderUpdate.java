package com.example.dto.websocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.dto.response.OrderItemResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdate {
    private Integer orderId;
    private Integer tableId;
    private String status;
    private BigDecimal totalPrice;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private List<OrderItemResponse> items;
}
