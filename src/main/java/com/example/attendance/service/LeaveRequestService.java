package com.example.attendance.service;

import com.example.attendance.entity.LeaveRequest;
import java.util.List;

public interface LeaveRequestService {
    // 学生申请请假
    LeaveRequest applyLeave(LeaveRequest request);

    // 学生获取自己的请假历史
    List<LeaveRequest> getStudentLeaves(String studentId);

    // 老师获取所有待审批的申请
    List<LeaveRequest> getPendingLeaves();

    // 老师审批 (status 传入 APPROVED 或 REJECTED)
    void approveLeave(Long requestId, String status);
    List<LeaveRequest> getProcessedLeaves();
    List<LeaveRequest> getPendingLeavesByTeacher(Long teacherId);
    List<LeaveRequest> getProcessedLeavesByTeacher(Long teacherId);
}