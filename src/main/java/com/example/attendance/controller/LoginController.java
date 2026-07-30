package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.StudentService;
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
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepository userRepository;
    // 1. 跳转到登录页面
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "用户名或密码错误！");
        }
        return "login";
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

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // 👈 核心修改：判断用户是否已认证（且排除匿名用户）
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            // 1. 判断是否是【学生】
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
                AttendanceStatisticsDTO stats = statisticsService.getStudentStats(username);
                model.addAttribute("stats", stats);
                model.addAttribute("role", "STUDENT");

                // 2. 判断是否是【教师】
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"))) {
                // 教师：展示该教师所教课程的考勤饼图
                AttendanceStatisticsDTO teacherStats = statisticsService.getTeacherStats(user.getId());
                model.addAttribute("stats", teacherStats);

                // 👈 【核心修复 1】：将全校学生数，替换为【该老师自己班级里的学生去重总数】！
                long myStudentsCount = studentService.countByTeacherId(user.getId()); // 记得在 StudentService 里写个中转方法调用 studentRepository.countByTeacherId(teacherId)
                model.addAttribute("totalStudents", myStudentsCount);

                model.addAttribute("role", "TEACHER");
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                model.addAttribute("role", "ADMIN");
                model.addAttribute("totalUsers", userRepository.count());
                model.addAttribute("totalCourses", courseService.getAllCourses().size());
                model.addAttribute("totalStudents", studentService.getAllStudents().size());
            }
        } else {
            // 👈 如果未登录，设置角色为 GUEST，不进行强制重定向
            model.addAttribute("role", "GUEST");
        }

        return "dashboard";
    }
}