package com.example.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;

//import com.example.restaurant.entity.Category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private boolean available;
    private Integer categoryId;
}
