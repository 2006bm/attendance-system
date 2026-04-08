package com.example.attendance.service;

import com.example.attendance.entity.User;
import java.util.List;

public interface UserService {
    String saveUser(User user);
    User getById(Long id);
    List<User> getAllTeachers();
    String updateUser(User user);
    String deleteUser(Long id);
}