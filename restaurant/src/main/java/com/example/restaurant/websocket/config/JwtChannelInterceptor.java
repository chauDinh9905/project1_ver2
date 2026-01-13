package com.example.restaurant.websocket.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.restaurant.security.JwtTokenProvider;

import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearerToken = authHeaders.get(0);
                if (bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);

                    if (jwtTokenProvider.validateToken(token)) {
                        String username = jwtTokenProvider.getUsernameFromToken(token);

                        // Tạo authentication đơn giản (không cần full UserDetails nếu chỉ check role)
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null
                        );

                        accessor.setUser(auth);
                        // Optional: set vào SecurityContext nếu cần dùng ở nơi khác
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
            // Nếu không có hoặc token invalid thì vẫn connect được (guest, user = null), thử thôi .___.
        }

        return message;
    }
}