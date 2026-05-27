package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "course") // 👈 对应 PostgreSQL 中的 course 表
public class Course {

    @Id
    @Column(name = "course_id", length = 20)
    private String courseId; // 课程编号 (主键)

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "classroom_name", length = 50)
    private String classroomName;

    private Short rows;
    private Short cols;

    @Column(name = "exclude_seats", length = 200)
    private String excludeSeats;

    private Short weekday;

    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "end_week")
    private Integer endWeek;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 👈 刚刚新增的两个时间限制字段
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // --- 必须有的无参构造方法 ---
    public Course() {
    }

    // --- Getter 和 Setter ---
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }
    public Short getRows() { return rows; }
    public void setRows(Short rows) { this.rows = rows; }
    public Short getCols() { return cols; }
    public void setCols(Short cols) { this.cols = cols; }
    public String getExcludeSeats() { return excludeSeats; }
    public void setExcludeSeats(String excludeSeats) { this.excludeSeats = excludeSeats; }
    public Short getWeekday() { return weekday; }
    public void setWeekday(Short weekday) { this.weekday = weekday; }
    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}