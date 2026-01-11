package com.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTableRequest {
    @NotNull
    @Min(value = 4, message = "Sức chứa của bàn ít nhất phải là 4 người")
    private Integer capacity;

    private String status = "AVAILABLE";
}
