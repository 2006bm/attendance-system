package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 提交考勤记录
    @PostMapping("/signin")
    public Result<Attendance> signIn(@RequestBody Attendance attendance) {
        Attendance saved = attendanceService.signIn(attendance);
        return Result.success(saved);
    }

    // 查询某个学生的考勤历史
    @GetMapping("/history/{studentId}")
    public Result<List<Attendance>> getHistory(@PathVariable String studentId) {
        List<Attendance> history = attendanceService.getStudentHistory(studentId);
        return Result.success(history);
    }
}