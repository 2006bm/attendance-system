package com.example.attendance.util;

import java.time.LocalDateTime;

public class SignInSession {
    private String courseId;
    private String code; // 4位动态码
    private LocalDateTime expireTime; // 过期时间

    public SignInSession(String courseId, String code, int durationMinutes) {
        this.courseId = courseId;
        this.code = code;
        this.expireTime = LocalDateTime.now().plusMinutes(durationMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    public String getCode() {
        return code;
    }
}