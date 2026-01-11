package com.example.restaurant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restaurant.entity.Category;
import com.example.restaurant.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;





@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping("/all-categories")
    public List<Category> getAllCategory() {
        return categoryService.getAllCategory();
    }
    
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Integer id){
        return categoryService.getCategoryById(id);
    }
    
    @GetMapping("/search")
    public Category getCategoryByName(@RequestParam String name){
        return categoryService.getCategoryByName(name);
    }
}
