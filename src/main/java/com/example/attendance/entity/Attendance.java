package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance") // 对应数据库表名
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    private String status; // NORMAL, LATE, etc.
    private Byte seatRow; // 座位行
    private Byte seatCol; // 座位列
    // --- 构造方法 ---
    public Attendance() {}

    // --- Getter 和 Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // 👈 任务二新增：座位行和列的 Getter/Setter
    public Byte getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(Byte seatRow) {
        this.seatRow = seatRow;
    }

    public Byte getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(Byte seatCol) {
        this.seatCol = seatCol;
    }
}