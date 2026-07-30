package com.example.attendance.scheduler;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.CourseSelection;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class AttendanceScheduler {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // 👈 核心：每 10 秒自动运行一次后台巡逻 (10000 毫秒 = 10 秒)
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void autoCheckAbsence() {
        LocalTime nowTime = LocalTime.now();

        // 1. 找出当前所有已经下课的课程 (下课时间早于当前时间)
        int todayWeekday = java.time.LocalDate.now().getDayOfWeek().getValue();
        List<Course> endedCourses = courseRepository.findByWeekdayAndEndTimeBefore(todayWeekday, nowTime);

        for (Course course : endedCourses) {
            // 2. 找到这门课名下的所有选课学生名单
            List<CourseSelection> roster = courseSelectionRepository.findByCourseId(course.getCourseId());

            // 今天的开始时间 (00:00:00) 和结束时间 (23:59:59)
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

            for (CourseSelection student : roster) {
                // 3. 【核心判定】：检查这个学生今天这门课有没有任何打卡或请假记录？
                boolean hasRecord = attendanceRepository.existsByStudentIdAndCourseIdAndCheckInTimeBetween(
                        student.getStudentId(),
                        course.getCourseId(),
                        todayStart,
                        todayEnd
                );

                // 4. 如果今天他既没有打卡，又没有请假：系统自动判定为 ❌ 缺勤 (ABSENT)！
                if (!hasRecord) {
                    Attendance absentRecord = new Attendance();
                    absentRecord.setStudentId(student.getStudentId());
                    absentRecord.setStudentName(student.getStudentName());
                    absentRecord.setCourseId(course.getCourseId());
                    absentRecord.setCheckInTime(LocalDateTime.now());
                    absentRecord.setStatus("ABSENT"); // 👈 自动扣上缺勤帽子

                    attendanceRepository.save(absentRecord);

                    System.out.println("【自动考勤系统】检测到学号：" + student.getStudentId()
                            + "，在课程：" + course.getCourseName() + " 中未打卡，已自动标记为【缺勤】！");
                }
            }
        }
    }
}