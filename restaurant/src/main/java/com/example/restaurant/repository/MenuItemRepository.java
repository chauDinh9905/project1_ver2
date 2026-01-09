package com.example.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restaurant.entity.MenuItem;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
   // Lấy món theo category (dùng cho tab menu)
    List<MenuItem> findByCategoryIdOrderByNameAsc(Integer categoryId);

    // Lấy tất cả món đang available, sắp xếp theo category -> name
    List<MenuItem> findByAvailableTrueOrderByCategory_SortOrderAscNameAsc();

    // Lấy tất cả món (bao gồm cả không available, dùng cho admin)
    List<MenuItem> findAllByOrderByCategory_SortOrderAscNameAsc();
    // tìm món ăn theo tên
    Optional<MenuItem> findByName(String name);
}
