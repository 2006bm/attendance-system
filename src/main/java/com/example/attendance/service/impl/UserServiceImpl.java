package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 👈 【核心修复 1】：如果前端没传真实姓名，默认用“用户名”代替，绝对防止数据库报错！
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            user.setRealName(request.getUsername());
        } else {
            user.setRealName(request.getRealName());
        }

        // 👈 【核心修复 2】：普通用户自助注册，角色一律默认设为 "STUDENT" (学生)
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            user.setRole("STUDENT");
        } else {
            user.setRole(request.getRole());
        }

        // 3. 将明文密码加密后存储
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // 4. 保存到数据库
        userRepository.save(user);

        return "注册成功";
    }
    @Override
    @Transactional
    public String updatePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 1. 👈 使用 matches 安全对比输入的旧密码与数据库里的密文
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码输入错误！");
        }

        // 2. 将新密码加密后存入数据库
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "密码修改成功，下次登录生效";
    }
}