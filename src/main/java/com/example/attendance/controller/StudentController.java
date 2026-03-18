package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Student;
import org.springframework.web.bind.annotation.*;



import java.util.Arrays;
import java.util.List;

@RestController//处理网页请求的控制器类
public class StudentController {
        // 编写一个GET接口，路径为 /student/info
        // @GetMapping 就是用来处理 GET 请求的，括号里访问的路径
        @GetMapping("/student/info")
        public String getStudentInfo() {
            // 返回的学生信息
            String info = "姓名：边苗，学号：42411085，班级：计算机科学与技术";

            // 返回这串文本给浏览器
            return info;
        }

        @PostMapping("/student/attendance")//@RequestBody String studentId
        public String postStudentAttendance(@RequestBody String studentId) {
            return "学号为"+studentId+"的学生打卡成功!";
        }

        @GetMapping("/student/courses")
        public List<String> getStudentCourses() {
            List<String> courses = Arrays.asList("JavaEE","数据库原理与应用","机器学习与数据挖掘","艺术导论");

            return courses;
        }

        @GetMapping("/student/info/{studentId}")
        public Result<Student> getStudentById(@PathVariable String studentId) {
                Student student=new Student(studentId,"边苗",20,"计算机科学与技术3班");
                return Result.success(student);

        }
}



