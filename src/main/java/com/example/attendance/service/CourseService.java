package com.example.attendance.service;

import com.example.attendance.entity.Course;
import java.util.List;

public interface CourseService {
    // 定义获取学生选课列表的方法
    List<Course> findByStudentId(String studentId);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> getAllCourses();
}