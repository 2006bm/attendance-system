package com.example.attendance.service.impl;

import com.example.attendance.dto.AttendanceStatisticsDTO;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public AttendanceStatisticsDTO getStudentStats(String studentId) {
        long total = attendanceRepository.countByStudentId(studentId);
        long normal = attendanceRepository.countByStudentIdAndStatus(studentId, "正常");
        long late = attendanceRepository.countByStudentIdAndStatus(studentId, "迟到");
        long absent = attendanceRepository.countByStudentIdAndStatus(studentId, "缺勤");
        long leave = attendanceRepository.countByStudentIdAndStatus(studentId, "早退");

        // 计算出勤率：(正常 + 迟到 + 早退) / 总数 * 100%
        double rate = 0.0;
        if (total > 0) {
            rate = ((double) (normal + late + leave) / total) * 100;
            // 保留一位小数
            rate = Math.round(rate * 10) / 10.0;
        }

        return new AttendanceStatisticsDTO(total, normal, late, absent, leave, rate);
    }
}