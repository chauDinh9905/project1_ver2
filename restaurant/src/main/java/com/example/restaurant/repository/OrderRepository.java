package com.example.restaurant.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restaurant.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByTableIdAndStatusNotIn(Integer tableId, List<String> excludedStatuses);

    // Lấy tất cả đơn của một bàn (dùng cho lịch sử nếu cần sau)
    List<Order> findByTableIdOrderByCreateAtDesc(Integer tableId);
}
