package com.example.attendance.service;

import com.example.attendance.entity.Student;
import com.example.attendance.util.ImportResult;
import jakarta.validation.Valid;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface StudentService {
    Page<Student> findAll(Pageable pageable);
    String createStudent(Student student);
    Student getStudentById(String studentId);
    Student findById(Long id);
    void save(@Valid Student student);

    Student findByStudentId(String studentId);

    List<Student> getAllStudents();
    ImportResult importStudents(String filePath, String courseId);
    void deleteStudent(Long id);
    Page<Student> getStudentsByTeacher(Long teacherId, Pageable pageable);
    long countByTeacherId(Long teacherId);
}
