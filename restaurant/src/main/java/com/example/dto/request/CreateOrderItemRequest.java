package com.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateOrderItemRequest {
    @NotNull(message = "id của món ăn không được để trống")
    private Integer menuItemId;

    @NotNull(message = "Số lượng món ăn không được để trống")
    @Min(value = 1, message = "Số lượng ít nhất là 1")
    private Integer quantity;

    @Size(max = 500, message = "ghi chú không được quá 500 ký tự")
    private String notes;
}
