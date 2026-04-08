package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 新增
    @PostMapping("/create")
    public Result<String> create(@RequestBody User user) {
        return Result.success(userService.saveUser(user));
    }

    // 查询单个
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    // 查询教师列表
    @GetMapping("/teachers")
    public Result<List<User>> getTeachers() {
        return Result.success(userService.getAllTeachers());
    }

    // 更新
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        return Result.success(userService.updateUser(user));
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }
}