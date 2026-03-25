package com.example.attendance.dao;


import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;



@Repository
public class StudentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Student student) {
        String sql="INSERT INTO student(student_id, name, class_name) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, student.getStudentId(), student.getStudentName(), student.getClassName());

    }

    public Student findById(String studentId) {
        String sql = "SELECT * FROM student WHERE student_id = ?";
        // queryForObject 用来查询单条数据，BeanPropertyRowMapper 会自动把数据库里的列和 Student 里的属性对应起来
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Student.class), studentId);
    }
}
