package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import com.example.attendance.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository; // 👈 引入考勤仓库，用于联动自动签到

    @Override
    public LeaveRequest applyLeave(LeaveRequest request) {
        request.setCreateTime(LocalDateTime.now());
        request.setStatus("PENDING"); // 默认是待处理
        return leaveRequestRepository.save(request);
    }

    @Override
    public List<LeaveRequest> getStudentLeaves(String studentId) {
        return leaveRequestRepository.findByStudentId(studentId);
    }

    @Override
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    @Override
    @Transactional // 👈 开启声明式事务，确保“修改请假状态”和“自动生成考勤”在同一个事务里成功
    public void approveLeave(Long requestId, String status) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("未找到该请假申请"));

        // 1. 修改请假记录状态
        request.setStatus(status);
        leaveRequestRepository.save(request);

        // 2. 👈 【核心联动】如果老师点击了“同意”，系统自动帮学生在考勤表里写一条“请假”记录！
        if ("APPROVED".equals(status)) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(request.getStudentId());
            attendance.setStudentName(request.getStudentName());
            attendance.setCourseId(request.getCourseId());
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setStatus("LEAVE"); // 👈 状态标记为 LEAVE (请假)

            attendanceRepository.save(attendance);
        }
    }

    @Override
    public List<LeaveRequest> getProcessedLeaves() {
        return leaveRequestRepository.findByStatusNot("PENDING"); // 查出所有非待审批（即已同意/已拒绝）的单子
    }

    @Override
    public List<LeaveRequest> getPendingLeavesByTeacher(Long teacherId) {
        return leaveRequestRepository.findPendingByTeacherId(teacherId);
    }

    @Override
    public List<LeaveRequest> getProcessedLeavesByTeacher(Long teacherId) {
        return leaveRequestRepository.findProcessedByTeacherId(teacherId);
    }
}