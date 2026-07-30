package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    @Query(value = "SELECT * FROM course WHERE teacher_id = ?1", nativeQuery = true)
    List<Course> findByTeacherId(Long teacherId);

    // 👈 替换为此处的原生 SQL 多表联查
    @Query(value = "SELECT c.* FROM course c " +
            "INNER JOIN course_selection cs ON c.course_id = cs.course_id " +
            "WHERE cs.student_id = ?1", nativeQuery = true)
    List<Course> findByStudentId(String studentId);

    List<Course> findByWeekdayAndEndTimeBefore(Integer weekday, LocalTime time);
}