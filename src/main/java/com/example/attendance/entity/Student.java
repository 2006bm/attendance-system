package com.example.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_info")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "class_name")
    private String className;

    private String gender;

    // 👈 任务二新增：完全对齐 Excel 的三个新字段
    private String college; // 学院
    private Integer grade;  // 年级
    private String major;   // 专业

    // --- 空构造方法 ---
    public Student() {}

    // --- 自动生成的 Getter 和 Setter (请根据这几个字段补齐) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
}