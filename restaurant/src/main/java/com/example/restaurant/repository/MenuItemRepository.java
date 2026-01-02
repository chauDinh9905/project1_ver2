package com.example.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restaurant.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
   // Lấy món theo category (dùng cho tab menu)
    List<MenuItem> findByCategoryIdOrderByNameAsc(Integer categoryId);

    // Lấy tất cả món đang available, sắp xếp theo category → name (tối ưu cho frontend)
    List<MenuItem> findByAvailableTrueOrderByCategory_SortOrderAscNameAsc();

    // Lấy tất cả món (bao gồm cả không available, dùng cho admin)
    List<MenuItem> findAllByOrderByCategory_SortOrderAscNameAsc();
}
