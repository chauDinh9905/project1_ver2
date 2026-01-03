package com.example.dto.response;

import java.math.BigDecimal;
//import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private Integer tableId;
    private String status;
    private BigDecimal totalPrice;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    List<OrderItemResponse> orderItems;
}
