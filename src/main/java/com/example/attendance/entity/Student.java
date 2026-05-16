package com.example.attendance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.processing.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity // 作用：标记这是一个实体类，JPA 会自动将其与数据库表关联
@Table(name = "student_info") // 作用：指定对应的数据库表名（假设你的表名叫 student_info）
public class Student {

    @Id // 作用：标记此属性为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 作用：设置主键自增策略（对应 PostgreSQL 的 SERIAL）
    private Long id;

    // 作用：配置字段映射。name 对应数据库列名，nullable=false 表示不能为空
    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "age")
    private Integer age;



    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "出生日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 确保日期格式正确
    private LocalDate birthday;

    @NotBlank(message = "联系方式不能为空")
    //@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    public Student(Long id, String studentId, String studentName, String className, Integer age, String gender, LocalDate birthday, String phone) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.age = age;
        this.gender = gender;
        this.birthday = birthday;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // --- 构造方法 ---
    public Student() {} // JPA 要求必须有一个无参构造方法！



}