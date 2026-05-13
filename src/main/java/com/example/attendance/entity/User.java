package com.example.attendance.entity;

import jakarta.persistence.Table;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity  // <<--- 就是缺了这个！没有它，JPA 就不管这个类
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String realName; // 注意：对应数据库列 real_name，RowMapper会自动转换
    private String role;
    private Timestamp createTime;

    // 无参构造方法（必须有！）
    public User() {}

    // Getter 和 Setter 方法（必须有！）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}