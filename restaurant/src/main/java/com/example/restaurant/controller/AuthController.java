package com.example.restaurant.controller;

import com.example.dto.request.RegisterUserRequest;
import com.example.dto.response.UserResponse;
import com.example.restaurant.entity.User;
import com.example.restaurant.repository.UserRepository;
import com.example.restaurant.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Xử lý Login
 * SỬ DỤNG LẠI: RegisterUserRequest và UserResponse
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    /**
     * Login cho admin
     * POST /api/auth/login
     * 
     * Dùng RegisterUserRequest (có userName + password)
     * Trả về UserResponse (có thêm token)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody RegisterUserRequest loginRequest) {
        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserName(),  // ← Dùng getUserName() thay vì getUsername()
                    loginRequest.getPassword()
                )
            );

            // Lấy user từ DB
            User user = userRepository.findByUserName(loginRequest.getUserName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate token
            String token = tokenProvider.generateToken(user.getUserName());

            // Lấy role
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority()
                    .replace("ROLE_", "");

            // Response - Dùng UserResponse với token
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .role(role)
                    .token(token)  // ← Token ở đây
                    .build();

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Tên đăng nhập hoặc mật khẩu không đúng");
        }
    }
}