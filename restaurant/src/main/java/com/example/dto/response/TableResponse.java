package com.example.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
//import lombok.NoArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponse {
    private Integer id;
    private Integer capacity;
    private String status;
    private Integer currentOrderId;
    private BigDecimal totalPrice;
}
