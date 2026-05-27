package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository; // 👈 记得注入这个
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Attendance signIn(Attendance attendance) {
        // 1. 获取课程信息
        Course course = courseRepository.findById(attendance.getCourseId())
                .orElseThrow(() -> new RuntimeException("未找到该课程！"));

        LocalTime nowTime = LocalTime.now(); // 当前时刻
        LocalTime classStart = course.getStartTime(); // 上课时间 (如 08:00)

        // 2. 时间限制校验：只能在 [上课前15分钟, 上课后30分钟] 内打卡
        LocalTime allowedStart = classStart.minusMinutes(15); // 07:45
        LocalTime allowedEnd = classStart.plusMinutes(30);   // 08:30

        if (nowTime.isBefore(allowedStart) || nowTime.isAfter(allowedEnd)) {
            // 抛出异常，阻止打卡，并带上失败提示
            throw new RuntimeException("打卡失败：当前不在打卡时间范围内（允许时间："
                    + allowedStart + " ~ " + allowedEnd + "）");
        }

        // 3. 自动判定迟到：超过上课时刻，设为迟到
        if (nowTime.isAfter(classStart)) {
            attendance.setStatus("LATE"); // 迟到
        } else {
            attendance.setStatus("NORMAL"); // 正常出勤
        }

        attendance.setCheckInTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance signOut(String studentId, String courseId) {
        // 1. 找到该学生今天这门课的签到记录
        // (为了简单，这里模拟找最新的一条记录。真实开发需要按日期和学生、课程定位)
        List<Attendance> records = attendanceRepository.findByStudentId(studentId);
        Attendance attendance = records.stream()
                .filter(r -> r.getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("签退失败：未找到今日签到记录！"));

        Course course = courseRepository.findById(courseId).orElseThrow();
        LocalTime nowTime = LocalTime.now();
        LocalTime classEnd = course.getEndTime(); // 下课时间 (如 09:30)

        // 2. 判定早退：如果当前时间早于下课时间，标记为早退
        if (nowTime.isBefore(classEnd)) {
            attendance.setStatus("EARLY_LEAVE"); // 早退
        }

        // 3. 保存更改
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getStudentHistory(String studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public Page<Attendance> getFilteredPage(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    @Override
    public List<Attendance> getFilteredList(Specification<Attendance> spec) {
        return attendanceRepository.findAll(spec);
    }
}