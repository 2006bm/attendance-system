package com.example.attendance.service.impl;

import com.example.attendance.entity.Course;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service // 👈 别忘了贴上大厨标签！
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Course> findByStudentId(String studentId) {
        // 调用我们刚刚写了 SQL 的仓库方法
        return courseRepository.findByStudentId(studentId);
    }
}