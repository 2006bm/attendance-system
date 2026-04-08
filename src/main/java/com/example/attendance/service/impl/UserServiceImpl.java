package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

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
}