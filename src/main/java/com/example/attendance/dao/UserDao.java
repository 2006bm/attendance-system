package com.example.attendance.dao;

import com.example.attendance.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// 1. 使用 @Repository 注解
@Repository
public class UserDao {

    // 2. 注入 JdbcTemplate
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 3. 实现 insert 方法 (新增教师用户)
    public void insert(User user) {
        // 注意：PostgreSQL 表名 user 必须加双引号
        String sql = "INSERT INTO \"user\" (username, password, real_name, role) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                "TEACHER" // 强制设置为教师角色
        );
    }

    // 4. 实现 findById 方法 (根据ID查询)
    public User findById(Long id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        // 使用 BeanPropertyRowMapper 自动映射结果
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
    }

    // 5. 实现 findByUsername 方法 (用于登录验证)
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        // 如果查不到会报 EmptyResultDataAccessException，实际开发建议加 try-catch 或用 list 接收
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
    }

    // 6. 实现 findAllTeachers 方法 (查询所有教师)
    public List<User> findAllTeachers() {
        String sql = "SELECT * FROM \"user\" WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    // 7. 实现 update 方法 (更新用户)
    public void update(User user) {
        String sql = "UPDATE \"user\" SET password = ?, real_name = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getId()
        );
    }

    // 8. 实现 deleteById 方法 (删除用户)
    public void deleteById(Long id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}