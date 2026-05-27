package com.example.attendance.service;

import com.example.attendance.entity.Student;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StudentService {
    Page<Student> findAll(Pageable pageable);
    String createStudent(Student student);
    Student getStudentById(String studentId);
    Student findById(Long id);
    void save(@Valid Student student);

    Student findByStudentId(String studentId);
}
