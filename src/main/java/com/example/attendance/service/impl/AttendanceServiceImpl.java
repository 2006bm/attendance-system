package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Attendance signIn(Attendance attendance) {
        // 设置签到时间为当前系统时间
        attendance.setCheckInTime(LocalDateTime.now());
        // 保存到数据库
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getStudentHistory(String studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
}