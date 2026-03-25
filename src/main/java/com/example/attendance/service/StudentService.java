package com.example.attendance.service;

import com.example.attendance.entity.Student;

public interface StudentService {
    String createStudent(Student student);
    Student getStudentById(String studentId);
}
