package com.example.restaurant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.restaurant.entity.User;
import com.example.restaurant.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id){
        return userService.getUserById(id);
    }

    @GetMapping("/search")
    public User getUserByName(@RequestParam String name){
        return userService.getUserByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User U){
        return userService.createUser(U.getUserName(), U.getPassword());
    }

    @PutMapping("/{id}")  
    public User updateUser(@PathVariable Integer id, @RequestBody User U){
        return userService.updateUser(id, U);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id){
        userService.deleteUser(id);
    }
}
