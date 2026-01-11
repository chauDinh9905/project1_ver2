package com.example.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateOrderItemRequest {
    @NotNull(message = "id của món ăn không được để trống")
    private Integer menuItemId;

    @NotNull(message = "số lượng món ăn không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu phải là 1")
    private Integer quantity;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String notes;
}
