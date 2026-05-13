package com.example.attendance.entity;

public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private String role; // 如：STUDENT 或 TEACHER

    // --- Getter 和 Setter (必须有) ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}