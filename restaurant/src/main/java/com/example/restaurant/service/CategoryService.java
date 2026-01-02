package com.example.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.restaurant.entity.Category;
import com.example.restaurant.repository.CategoryRepository;


@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }
    public Category getCategoryById(Integer id){
        return categoryRepository.findById(id).orElse(null);
    }
    public Category getCategoryByName(String name){
        return categoryRepository.findByName(name).orElse(null); 
    }
    public List<Category> getAllCategory(){
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }
    public boolean existsById(Integer id){
        return categoryRepository.existsById(id);
    }
    public boolean existsByName(String name){
        return categoryRepository.existsByName(name);
    }
}
