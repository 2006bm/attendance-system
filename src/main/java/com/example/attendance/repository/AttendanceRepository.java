package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> , JpaSpecificationExecutor<Attendance> {

    // 定义关联查询方法：根据学号查询该学生的所有考勤记录
    List<Attendance> findByStudentId(String studentId);

    // 也可以根据课程查询
    List<Attendance> findByCourseId(String courseId);
    // 1. 系统自带了 findAll(Pageable pageable)，所以不需要重复写查询所有的

    // 2. 如果要按学号分页查询，就这样写：
    Page<Attendance> findByStudentId(String studentId, Pageable pageable);

    long countByStudentId(String studentId);

    // 👈 2. 自动统计该学生指定状态的打卡次数 (如 正常、迟到、早退、缺勤)
    long countByStudentIdAndStatus(String studentId, String status);
    long countByStatus(String status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.courseId IN (SELECT c.courseId FROM Course c WHERE c.teacherId = :teacherId)")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.status = :status AND a.courseId IN (SELECT c.courseId FROM Course c WHERE c.teacherId = :teacherId)")
    long countByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") String status);

    boolean existsByStudentIdAndCourseIdAndCheckInTimeBetween(
            String studentId,
            String courseId,
            LocalDateTime start,
            LocalDateTime end
    );

}