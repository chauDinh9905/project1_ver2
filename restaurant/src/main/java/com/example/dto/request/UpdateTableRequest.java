package com.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTableRequest {
    @NotNull
    @Min(value = 4, message = "Sức chứa của bàn phải từ 4 người trở lên")
    private Integer capacity;
    
    @NotNull(message = "Không được để trống trạng thái bàn")
    private String status;
}
