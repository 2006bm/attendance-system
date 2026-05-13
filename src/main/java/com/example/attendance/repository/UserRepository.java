package com.example.attendance.repository;

import com.example.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JPA 会根据方法名自动生成 SQL: SELECT * FROM user WHERE username = ?
    Optional<User> findByUsername(String username);
}