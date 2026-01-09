package com.example.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restaurant.entity.TableEntity;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    List<TableEntity> findAllByOrderByIdAsc();

    List<TableEntity> findAllByCapacity(Integer capacity);

    List<TableEntity> findAllByStatus(String status);
}
