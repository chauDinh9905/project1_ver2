package com.example.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restaurant.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByOrderBySortOrderAsc();
    
    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
