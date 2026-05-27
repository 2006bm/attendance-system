package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> { // 👈 主键类型是 String

    // 核心技术：使用 Native SQL 关联课程表和选课表，一键查出这个学生选的所有课
    @Query(value = "SELECT c.* FROM course c " +
            "JOIN course_selection cs ON c.course_id = cs.course_id " +
            "WHERE cs.student_id = ?1", nativeQuery = true)
    List<Course> findByStudentId(String studentId);
}