package com.example.attendance.controller;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // 确保导包正确
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller//处理网页请求的控制器类
@RequestMapping("/student")
public class StudentController {
//    // 编写一个GET接口，路径为 /student/info
//    // @GetMapping 就是用来处理 GET 请求的，括号里访问的路径
//    @GetMapping("/student/info")
//    public String getStudentInfo() {
//        // 返回的学生信息
//        String info = "姓名：边苗，学号：42411085，班级：计算机科学与技术";
//
//        // 返回这串文本给浏览器
//        return info;
//    }
//
//    @PostMapping("/student/attendance")//@RequestBody String studentId
//    public String postStudentAttendance(@RequestBody String studentId) {
//        return "学号为" + studentId + "的学生打卡成功!";
//    }
//
//    @GetMapping("/student/courses")
//    public List<String> getStudentCourses() {
//        List<String> courses = Arrays.asList("JavaEE", "数据库原理与应用", "机器学习与数据挖掘", "艺术导论");
//
//        return courses;
//    }
//
//    @GetMapping("/student/info/{studentId}")
//    public Result<Student> getStudentById(@PathVariable String studentId) {
//        Student student = new Student(studentId, "边苗", 20, "计算机科学与技术3班");
//        return Result.success(student);
//
//    }
//
//    @GetMapping("/student/list")
//    public Result<List<Student>> getStudentList(@RequestParam String className,
//                                                @RequestParam(defaultValue = "1") Integer pageNum) {
//        System.out.println("接收到查询请求班级：" + className + ",页码：" + pageNum);
//        List<Student> studentList = new ArrayList<>();
//        studentList.add(new Student("2026001", "张三", 20, className));
//        studentList.add(new Student("2026002", "李四", 20, className));
//
//        return Result.success(studentList);
//    }
//
//    @PostMapping("/attendance/update")
//    public Result<String> updateStudentAttendance(@RequestBody AttendanceRecord record) {
//        System.out.println("后台已接收到考勤更新请求学号：" + record.getStudentId() + "，状态：" + record.getStatus());
//
//        // 2. 返回更新成功消息
//        String successMsg = "学号 " + record.getStudentId() + " 的考勤记录已更新为：" + record.getStatus();
//        return Result.success(successMsg);
//    }

    // 任务要求 2：使用 @Autowired 注入 Service 依赖
    // 意思：前台接待员关联了 Service 大厨
    @Autowired
    private StudentService studentService;

//    // 任务要求 4：新增学生接口（逻辑移至 Service 层）
//    @PostMapping("/create")
//    public Result<String> create(@RequestBody Student student) {
//        // Controller 啥都不管，直接甩给 Service 去做，并把结果装进 Result
//        String msg = studentService.createStudent(student);
//        return Result.success(msg);
//    }
//
//    // 任务要求 4：根据 ID 查询学生（逻辑移至 Service 层）
//    @GetMapping("/{id}")
//    public Result<Student> getById(@PathVariable String id) {
//        // 调用 Service 获取数据
//        Student student = studentService.getStudentById(id);
//        return Result.success(student);
//    }

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // 注意：PageRequest 页码从0开始，所以这里要 page - 1
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("studentId").ascending());
        Page<Student> studentPage = studentService.findAll(pageable); // 确保 Service 里调用了 repository.findAll(pageable)

        // 2. 传递这四个关键变量
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());

        return "student-list";// 返回 templates/student-list.html
    }

    // 1. 打开新增页面
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student()); // 给页面一个空的 Student 对象
        return "student-form";
    }

    // 2. 打开编辑页面 (回显数据)
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") Long id, Model model) {
        // 根据 ID 从数据库查出这个学生
        Student student = studentService.findById(id);
        if (student == null) {
            // 如果查不到，说明 ID 有误，建议重定向回列表或抛出异常
            return "redirect:/student/list";
        }
        model.addAttribute("student", student); // 把查到的学生塞给页面，HTML 会自动填充
        return "student-form";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student, BindingResult bindingResult) {
        // 如果有校验错误，返回表单页面
        if (bindingResult.hasErrors()) {
            return "student-form";
        }
        studentService.save(student);
        return "redirect:/student/list";
    }
}



