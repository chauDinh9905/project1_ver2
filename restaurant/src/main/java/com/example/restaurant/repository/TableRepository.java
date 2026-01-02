package com.example.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restaurant.entity.TableEntity;

public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    List<TableEntity> findAllByOrderByIdAsc();
}
