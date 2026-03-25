package com.example.attendance.service.impl;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentDao studentDao;

    @Override
    public String createStudent(Student student) {
        if(student.getStudentName()==null || student.getStudentName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        studentDao.insert(student);
        return "创建成功";
    }
    @Override
    public Student getStudentById(String studentId) {
        return studentDao.findById(studentId);
    }
}
