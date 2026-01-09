package com.example.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restaurant.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByOrderBySortOrderAsc(); // lấy loại món ăn theo thứ tự gọi
    
    Optional<Category> findByName(String name);  // tìm loại món ăn theo tên

    boolean existsByName(String name);   // kiểm tra xem loại món ăn đó có tồn tại không
}
