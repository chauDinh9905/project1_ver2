package com.example.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tables")
@Data

public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "capacity")
    private Integer capacity = 4;

    @Column(name = "status", length = 20)
    private String status = "AVAILABLE";
}
