package com.example.attendance;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController

public class AttendanceSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }

    @GetMapping("/hello")//浏览器里面接口地址
    public String hello() {
        return "欢迎来到班级考勤管理系统！";
    }

    @GetMapping("/about") // 定义接口的访问路径为 /about
    public String about() {
        // 自己的信息
        String myInfo = "姓名：边苗, 专业：计算机科学与技术";
        return myInfo;
    }
}