package com.example.attendance.entity;

public class Student {
    private String studentId;
    private String studentName;
    private String className;
    private Integer age;

    public Student(String studentId, String studentName, Integer age, String className) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.age = age;
        this.className = className;
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
}
