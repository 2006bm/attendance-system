package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.util.ImportResult;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttendanceService {
    Attendance signIn(Attendance attendance);

    Attendance signOut(String studentId, String courseId);

    List<Attendance> getStudentHistory(String studentId);

    // 👈 新增这两个支持动态查询的方法
    Page<Attendance> getFilteredPage(Specification<Attendance> spec, Pageable pageable);
    List<Attendance> getFilteredList(Specification<Attendance> spec);

    ImportResult importFromExcel(String filePath);
}