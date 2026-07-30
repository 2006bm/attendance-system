package com.example.attendance.service.impl;

import com.example.attendance.entity.Course;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Course> findByStudentId(String studentId) {
        List<Course> list = courseRepository.findByStudentId(studentId);

        // 👈 核心修复：如果是新注册的学生，选课表里没数据，默认让他能看到所有课，防止下拉框为空！
        if (list.isEmpty()) {
            return courseRepository.findAll();
        }
        return list;
    }
    @Override
    public List<Course> findByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }
    @Override
    public List<Course> getAllCourses() {
        // 调用 JPA 的库，查出系统里所有的课程
        return courseRepository.findAll();
    }
}