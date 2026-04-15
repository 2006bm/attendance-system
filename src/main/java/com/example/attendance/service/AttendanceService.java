package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import java.util.List;

public interface AttendanceService {
    Attendance signIn(Attendance attendance); // 签到
    List<Attendance> getStudentHistory(String studentId); // 查看历史
}