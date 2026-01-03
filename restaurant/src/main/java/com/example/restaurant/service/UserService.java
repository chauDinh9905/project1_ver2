package com.example.restaurant.service;

import org.springframework.stereotype.Service;

import com.example.restaurant.entity.User;
import com.example.restaurant.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository1){
        this.userRepository = userRepository1;
    }

    public User getUserById(Integer id){
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("id của admin không hợp lệ"));
    }

    public User getUserByName(String name){
        return userRepository.findByUserName(name).orElseThrow(()->new RuntimeException("tên đăng nhập admin không đúng"));
    }

    public User createUser(String userName, String password){
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        // Mặc định role nếu entity chưa set
        if (user.getRole() == null) {
            user.setRole("ADMIN");
        }

        return userRepository.save(user);
    }

    public User updateUser(Integer userId, User newUser){
       User oldUser = getUserById(userId);
       if(newUser.getUserName() != null){
          oldUser.setUserName(newUser.getUserName());
       }
       if(newUser.getPassword() != null){
          oldUser.setPassword(newUser.getPassword());
       }
       if (newUser.getRole() != null) {
            oldUser.setRole(newUser.getRole());
        }
       return userRepository.save(oldUser);
    }

    public boolean existsByUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }
}
