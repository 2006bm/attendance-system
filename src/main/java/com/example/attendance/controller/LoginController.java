package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @Autowired
    private com.example.attendance.service.AttendanceStatisticsService statisticsService;
    @Autowired
    private StudentRepository studentRepository;
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
    public String dashboard(Authentication authentication, Model model) {
        // 获取当前登录用户名
        String username = authentication.getName();

        // 1. 判断是否是学生身份 (Spring Security 角色在底层会带上 ROLE_ 前缀)
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
            // 学生：获取个人统计
            AttendanceStatisticsDTO stats = statisticsService.getStudentStats(username);
            model.addAttribute("stats", stats);
            model.addAttribute("role", "STUDENT"); // 塞入角色标识
        } else {
            // 2. 教师或管理员：不进行个人统计，而是传递一些全局的管理数据
            // 比如：系统里目前共有多少个学生，有多少条考勤记录
            model.addAttribute("totalStudents", studentRepository.count());
            model.addAttribute("role", "TEACHER"); // 塞入角色标识
        }

        return "dashboard";
    }
}