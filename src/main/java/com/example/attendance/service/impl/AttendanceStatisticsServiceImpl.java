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

    // 1. 修改学生统计 getStudentStats 里的逻辑：
    @Override
    public AttendanceStatisticsDTO getStudentStats(String studentId) {
        long total = attendanceRepository.countByStudentId(studentId);
        long normal = attendanceRepository.countByStudentIdAndStatus(studentId, "NORMAL");
        long absent = attendanceRepository.countByStudentIdAndStatus(studentId, "ABSENT");
        long leave = attendanceRepository.countByStudentIdAndStatus(studentId, "EARLY_LEAVE");

        double rate = 0.0;
        if (total > 0) {
            // 👈 核心修改：出勤率 = (正常 + 早退) / 总数
            rate = ((double) (normal + leave) / total) * 100;
            rate = Math.round(rate * 10) / 10.0;
        }
        // 第三个参数（原本是lateCount）直接传入 0 即可，省去修改 DTO 类的麻烦
        return new AttendanceStatisticsDTO(total, normal, 0, absent, leave, rate);
    }

    // 2. 同样，修改教师全局统计 getClassGlobalStats 里的逻辑：
    @Override
    public AttendanceStatisticsDTO getClassGlobalStats() {
        long total = attendanceRepository.count();
        long normal = attendanceRepository.countByStatus("NORMAL");
        long absent = attendanceRepository.countByStatus("ABSENT");
        long leave = attendanceRepository.countByStatus("EARLY_LEAVE");

        double rate = 0.0;
        if (total > 0) {
            rate = ((double) (normal + leave) / total) * 100;
            rate = Math.round(rate * 10) / 10.0;
        }
        return new AttendanceStatisticsDTO(total, normal, 0, absent, leave, rate);
    }


    @Override
    public AttendanceStatisticsDTO getTeacherStats(Long teacherId) {
        long total = attendanceRepository.countByTeacherId(teacherId);
        long normal = attendanceRepository.countByTeacherIdAndStatus(teacherId, "NORMAL");
        long late = attendanceRepository.countByTeacherIdAndStatus(teacherId, "LATE");
        long absent = attendanceRepository.countByTeacherIdAndStatus(teacherId, "ABSENT");
        long leave = attendanceRepository.countByTeacherIdAndStatus(teacherId, "LEAVE");

        double rate = 0.0;
        if (total > 0) {
            rate = ((double) (normal + late + leave) / total) * 100;
            rate = Math.round(rate * 10) / 10.0;
        }
        return new AttendanceStatisticsDTO(total, normal, late, absent, leave, rate);
    }
}