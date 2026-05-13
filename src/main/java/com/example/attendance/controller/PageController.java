package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 注意：这里用 @Controller，不是 @RestController！
public class PageController {

    // 访问 /register 网址，返回 register.html 页面
    @GetMapping("/register")
    public String toRegisterPage() {
        return "register"; // 对应 templates/register.html
    }
}