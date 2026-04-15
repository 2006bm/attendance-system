package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // 定义关联查询方法：根据学号查询该学生的所有考勤记录
    List<Attendance> findByStudentId(String studentId);

    // 也可以根据课程查询
    List<Attendance> findByCourseId(String courseId);
}