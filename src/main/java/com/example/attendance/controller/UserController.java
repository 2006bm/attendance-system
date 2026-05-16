package com.example.attendance.controller;

import com.example.attendance.entity.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 新增
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public Result<String> create(@RequestBody User user) {
        return Result.success(userService.saveUser(user));
    }

    // 查询单个
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    // 查询教师列表
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @GetMapping("/teachers")
    public Result<List<User>> getTeachers() {
        return Result.success(userService.getAllTeachers());
    }

    // 更新
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        return Result.success(userService.updateUser(user));
    }

    // 删除
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }

    // 注册：去掉 @RequestBody，使用普通的对象接收参数
    @PostMapping("/user/register")
    public String register(RegisterRequest request) {
        try {
            userService.register(request);
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace(); // 在控制台打印具体的错误信息
            return "redirect:/register?error=true";
        }
    }

}