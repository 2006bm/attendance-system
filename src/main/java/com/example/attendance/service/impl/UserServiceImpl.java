package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder; // 注入加密器
    @Autowired
    private UserRepository userRepository;

    @Override
    public String saveUser(User user) {
        userDao.insert(user);
        return "用户新增成功";
    }

    @Override
    public User getById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public String updateUser(User user) {
        userDao.update(user);
        return "用户更新成功";
    }

    @Override
    public String deleteUser(Long id) {
        userDao.deleteById(id);
        return "用户删除成功";
    }


    @Override
    public String register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名 [" + request.getUsername() + "] 已存在！");
        }

        // 2. 创建新用户对象并设置信息
        User user = new User();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());

        // 3. 【核心步】将明文密码加密后存储
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // 4. 保存到数据库
        userRepository.save(user);

        return "注册成功";
    }
}