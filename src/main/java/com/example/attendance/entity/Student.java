package com.example.attendance.entity;

import jakarta.persistence.*;

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

    // --- 构造方法 ---
    public Student() {} // JPA 要求必须有一个无参构造方法！

    public Student(String studentId, String studentName, String className, Integer age) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.age = age;
    }

    // --- Getter 和 Setter ---
    // (这里保持你之前的 Getter 和 Setter 即可)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}