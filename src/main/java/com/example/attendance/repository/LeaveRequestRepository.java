package com.example.attendance.repository;

import com.example.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // 1. 学生查看自己的请假历史
    List<LeaveRequest> findByStudentId(String studentId);

    // 2. 老师查看待审批的请假申请
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByStatusNot(String status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' AND lr.courseId IN " +
            "(SELECT c.courseId FROM Course c WHERE c.teacherId = :teacherId)")
    List<LeaveRequest> findPendingByTeacherId(@Param("teacherId") Long teacherId);

    // 👈 2. 新增：通过三表联查，只找出【申请了该教师课程】且【状态为已处理】的假条历史
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status != 'PENDING' AND lr.courseId IN " +
            "(SELECT c.courseId FROM Course c WHERE c.teacherId = :teacherId)")
    List<LeaveRequest> findProcessedByTeacherId(@Param("teacherId") Long teacherId);
}