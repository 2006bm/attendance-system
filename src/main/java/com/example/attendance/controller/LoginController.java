package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // 1. 跳转到登录页面
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            // 如果有 error 参数，往页面丢一个错误提示
            model.addAttribute("errorMsg", "用户名或密码错误！");
        }
        return "login"; // 返回 templates/login.html
    }

    // 2. 模拟验证逻辑 (实际项目建议整合进 Security 的 UserDetailsService)
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // 这里只是为了演示“重定向”：假设用户名 admin 密码 123 成功
        if ("admin".equals(username) && "123".equals(password)) {
            return "redirect:/dashboard"; // 成功跳转到首页
        }
        return "redirect:/login?error=true"; // 失败重定向回登录页并带上 error 参数
    }

    // 3. 首页 (Dashboard)
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // 返回 templates/dashboard.html
    }
}