package com.example.restaurant.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restaurant.entity.Order;
//import com.example.restaurant.entity.TableEntity;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByTableIdAndStatusNotIn(Integer tableId, List<String> excludedStatuses);

    // Lấy tất cả đơn của một bàn (dùng cho lịch sử nếu cần sau)
    List<Order> findByTableIdOrderByCreateAtDesc(Integer tableId);
    List<Order> findByStatus(String status);
    Optional<Order> findByTableId(Integer tableId);
    Integer countByTableId(Integer tableId); // số lượng đơn của 1 bàn
}
