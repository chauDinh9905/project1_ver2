package com.example.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.service.MenuItemService;

@RestController
@RequestMapping("/api/menu-item")
@CrossOrigin(origins = "*")
public class MenuItemController {
    private final MenuItemService menuItemService;
    public MenuItemController(MenuItemService menuItemService){
        this.menuItemService = menuItemService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<MenuItem>> getAvailableMenuItems() {
        List<MenuItem> items = menuItemService.gettAllMenuItemAvailable();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public MenuItem getMenuItemById(@PathVariable Integer id){
        return menuItemService.getMenuItemById(id);
    }

    @GetMapping("/search")
    public MenuItem getMenuItemByName(@RequestParam String name){
        return menuItemService.getMenuItemByName(name);
    }

    @GetMapping("/all")
    public List<MenuItem> getAllMenuItems(){
        return menuItemService.getAllMenuItems();
    }

    @GetMapping("/all/{categoryId}")
    public List<MenuItem> getAllMenuItemByCategoryId(@PathVariable Integer categoryId){
        return menuItemService.getAllMenuItemByCategoryId(categoryId);
    }

    @GetMapping("/all/available")
    public List<MenuItem> gettAllMenuItemAvailable(){
        return menuItemService.gettAllMenuItemAvailable();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItem createMenuItem(@RequestBody MenuItem menuItem){
        return menuItemService.createMenuItem(menuItem);
    }

    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable Integer id, @RequestBody MenuItem MI){
        return menuItemService.updateMenuItem(id, MI);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Integer id){
        menuItemService.deleteMenuItem(id);
    }

    @PatchMapping("/{id}/toggle")
    public MenuItem toggleStatus(@PathVariable Integer id){
        return menuItemService.toggleAvailable(id);
    }
}
