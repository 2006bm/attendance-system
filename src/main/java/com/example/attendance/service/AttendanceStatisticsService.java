package com.example.attendance.service;
import com.example.attendance.dto.AttendanceStatisticsDTO;

public interface AttendanceStatisticsService {
    AttendanceStatisticsDTO getStudentStats(String studentId);
    AttendanceStatisticsDTO getClassGlobalStats();

    AttendanceStatisticsDTO getTeacherStats(Long teacherId);
}