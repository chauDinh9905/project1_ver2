package com.example.restaurant.service;

import java.util.List;


import org.springframework.stereotype.Service;

import com.example.restaurant.entity.Category;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.repository.CategoryRepository;
import com.example.restaurant.repository.MenuItemRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    public MenuItemService(MenuItemRepository menuItemRepository1, CategoryRepository categoryRepository1){
        this.menuItemRepository = menuItemRepository1;
        this.categoryRepository = categoryRepository1;
    }

    public MenuItem getMenuItemById(Integer id){
        return menuItemRepository.findById(id).orElseThrow(() -> new RuntimeException("id của món ăn không tồn tại"));
    }

    public MenuItem getMenuItemByName(String name){
        return menuItemRepository.findByName(name).orElseThrow(()-> new RuntimeException("không có món ăn nào tên như vậy"));
    }

    public List<MenuItem> getAllMenuItemByCategoryId(Integer categoryId){
        return menuItemRepository.findByCategoryIdOrderByNameAsc(categoryId);
    }

    public List<MenuItem> gettAllMenuItemAvailable(){
        return menuItemRepository.findByAvailableTrueOrderByCategory_SortOrderAscNameAsc();
    }

    public List<MenuItem> getAllMenuItems(){
        return menuItemRepository.findAllByOrderByCategory_SortOrderAscNameAsc();
    }

    @Transactional
    public MenuItem createMenuItem(MenuItem MI){
          if(MI.getName() == null || MI.getName().trim().isEmpty()){
            throw new IllegalArgumentException("Tên món không được để trống");
          }

          if(MI.getPrice() == null || MI.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Giá món ăn phải là số nguyên dương");
          }

          if(MI.getCategory() == null || MI.getCategory().getId() == null){
            throw new IllegalArgumentException("Bạn chưa điền danh mục loại đồ ăn (Khai vị, món chính, tráng miệng, đồ uống)");
          }

          Category category = categoryRepository.findById(MI.getCategory().getId()).orElseThrow(()-> new RuntimeException("danh mục id loại món ăn không hợp lệ (chỉ từ 1 đến 4)"));
          MI.setCategory(category);

          return menuItemRepository.save(MI);
    }

    @Transactional
    public MenuItem updateMenuItem(Integer id, MenuItem MI){
           MenuItem oldMI = getMenuItemById(id);
           if(MI.getName() != null && !MI.getName().trim().isEmpty()){
              oldMI.setName(MI.getName());
           }
           if(MI.getDescription() != null && !MI.getDescription().trim().isEmpty()){
              oldMI.setDescription(MI.getDescription());
           }
           if(MI.getPrice() != null && MI.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0){
              oldMI.setPrice(MI.getPrice());
           }
           if(MI.getImage() != null && !MI.getImage().trim().isEmpty()){
              oldMI.setImage(MI.getImage());
           }
           oldMI.setAvailable(MI.isAvailable());
           if(MI.getCategory() != null && MI.getCategory().getId() != null){
             Category newCategory = categoryRepository.findById(MI.getCategory().getId()).orElseThrow(()-> new RuntimeException("Danh mục loại món ăn không hợp lệ"));
             oldMI.setCategory(newCategory);
           }
           return menuItemRepository.save(oldMI);
    }

    @Transactional
    public void deleteMenuItem(Integer id){
        if(!menuItemRepository.existsById(id)){
            throw new RuntimeException("id của món ăn không tồn tại");
        }
        menuItemRepository.deleteById(id);
    }

    @Transactional
    public MenuItem toggleAvailable(Integer id){
        MenuItem oldMI = getMenuItemById(id);
        oldMI.setAvailable(!oldMI.isAvailable());

        return menuItemRepository.save(oldMI);
    }
}
