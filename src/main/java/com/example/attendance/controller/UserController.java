package com.example.attendance.controller;

import com.example.attendance.entity.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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


    // 👈 修改后的注册接口：无缝支持扫码后的一键打卡
    @PostMapping("/register")
    public String register(RegisterRequest request,
                           @RequestParam(required = false) String targetCourseId,
                           @RequestParam(required = false) String targetCode) {
        try {
            userService.register(request);

            // 注册成功：带着参数去登录页
            if (targetCourseId != null && !targetCourseId.isEmpty()
                    && targetCode != null && !targetCode.isEmpty()) {
                return "redirect:/login?targetCourseId=" + targetCourseId + "&targetCode=" + targetCode;
            }
            return "redirect:/login";
        } catch (Exception e) {
            // 注册失败：带着参数回退注册页
            if (targetCourseId != null && !targetCourseId.isEmpty()
                    && targetCode != null && !targetCode.isEmpty()) {
                return "redirect:/register?error=true&targetCourseId=" + targetCourseId + "&targetCode=" + targetCode;
            }
            return "redirect:/register?error=true";
        }
    }

    // 1. 跳转到修改密码页面
    @GetMapping("/password")
    public String passwordPage() {
        return "password-reset";
    }

    // 2. 接收修改密码表单
    @PostMapping("/password")
    public String updatePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的新密码不一致！");
            return "password-reset";
        }

        try {
            String username = authentication.getName();
            userService.updatePassword(username, oldPassword, newPassword);
            return "redirect:/user/password?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "password-reset";
        }
    }
}