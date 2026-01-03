package com.example.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer id;
    private String name;
    private Integer orderId;
    private Integer menuItemId;
    private Integer quantity;
    private BigDecimal price;
    private String notes;
}
