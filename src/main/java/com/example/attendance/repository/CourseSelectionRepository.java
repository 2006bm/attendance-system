package com.example.attendance.repository;

import com.example.attendance.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {

    // 👈 新增：判断某个学生是否已经选过这门课了，防止重复导入冲突
    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
    List<CourseSelection> findByCourseId(String courseId);
//    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
}