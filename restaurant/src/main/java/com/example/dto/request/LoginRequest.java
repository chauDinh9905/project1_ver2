package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String userName;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
