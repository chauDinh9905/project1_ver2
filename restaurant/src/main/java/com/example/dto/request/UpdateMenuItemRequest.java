package com.example.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateMenuItemRequest {
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 200, message = "Tên món ăn không được quá 200 ký tự")
    private String name;

    private String description;
      
    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "Giá của một món ăn phải lớn hơn không")
    private BigDecimal price;

    @Size(max = 500, message = "link ảnh không quá 500 ký tự")
    private String image;

    private boolean available = true;

    @NotNull(message = "id kiểu món ăn không được để trống (1: Khai vị, 2: Món chính, 3: Tráng miệng, 4: Đồ uống")
    private Integer categoryId;
}
