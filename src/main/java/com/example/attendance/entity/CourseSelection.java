package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_selection") // 对应 PostgreSQL 的选课关联表
public class CourseSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    private String gender;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "select_time")
    private LocalDateTime selectTime = LocalDateTime.now(); // 默认选课时间为当前系统时间

    // --- 构造方法 (必须有空构造) ---
    public CourseSelection() {
    }

    // --- Getter 和 Setter (必须有) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public LocalDateTime getSelectTime() { return selectTime; }
    public void setSelectTime(LocalDateTime selectTime) { this.selectTime = selectTime; }
}