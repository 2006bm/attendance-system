package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.LeaveRequestService;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    // 1. 【学生端】跳转到请假申请页面
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/apply")
    public String applyPage(Authentication authentication, Model model) {
        String studentId = authentication.getName();
        List<Course> courses = courseService.findByStudentId(studentId);

        model.addAttribute("courses", courses);
        model.addAttribute("leave", new LeaveRequest());

        // 传递请假历史，方便学生在页面下方看到自己之前的申请状态
        model.addAttribute("history", leaveRequestService.getStudentLeaves(studentId));
        return "leave-apply"; // 对应 templates/leave-apply.html
    }

    // 2. 【学生端】提交请假表单
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/apply")
    public String applySubmit(LeaveRequest leave, Authentication authentication) {
        String studentId = authentication.getName();
        Student s = studentService.findByStudentId(studentId);

        leave.setStudentId(studentId);
        leave.setStudentName(s.getStudentName());

        leaveRequestService.applyLeave(leave);
        return "redirect:/leave/apply?success=true";
    }

    // 3. 【教师端】跳转到请假审批管理页面
    @Autowired
    private com.example.attendance.repository.UserRepository userRepository;
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/approve")
    public String approvePage(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (isAdmin) {
            // 管理员：查看和处理全校所有的请假单
            model.addAttribute("pendingLeaves", leaveRequestService.getPendingLeaves());
            model.addAttribute("processedLeaves", leaveRequestService.getProcessedLeaves());
        } else {
            // 👈 【核心修复 2】：普通任课老师登录，只向前端推送【申请了自己课程】的假条！
            model.addAttribute("pendingLeaves", leaveRequestService.getPendingLeavesByTeacher(user.getId()));
            model.addAttribute("processedLeaves", leaveRequestService.getProcessedLeavesByTeacher(user.getId()));
        }
        return "leave-approve";
    }

    // 4. 【教师端】执行审批动作 (通过路径传参，非常优雅)
    @PreAuthorize("hasAnyRole('TEACHER')")
    @GetMapping("/action/{id}/{status}")
    public String handleLeave(@PathVariable Long id, @PathVariable String status) {
        leaveRequestService.approveLeave(id, status);
        return "redirect:/leave/approve";
    }


}