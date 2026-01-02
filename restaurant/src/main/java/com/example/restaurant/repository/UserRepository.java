package com.example.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restaurant.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
}
