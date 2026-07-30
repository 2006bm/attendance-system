package com.example.attendance.service;

import com.example.attendance.entity.RegisterRequest;
import com.example.attendance.entity.User;
import java.util.List;

public interface UserService {
    String saveUser(User user);
    User getById(Long id);
    List<User> getAllTeachers();
    String updateUser(User user);
    String deleteUser(Long id);

    String register(RegisterRequest request);
    String updatePassword(String username, String oldPassword, String newPassword);
}