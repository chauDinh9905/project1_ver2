package com.example.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Số nhận diện bàn không được để trống")
    private Integer tableId;

    //private String status = "PENDING";

    @NotEmpty(message = "Danh sách món không được rỗng")
    @Valid
    private List<OrderItemRequest> items;
    
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String notes;
}
