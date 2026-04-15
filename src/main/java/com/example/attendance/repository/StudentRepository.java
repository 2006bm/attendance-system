package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository 标记这是一个仓库组件
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // 继承了 JpaRepository 后，你已经默认拥有了：
    // save(student)      - 保存或更新
    // findAll()          - 查询所有
    // findById(id)       - 根据主键查询
    // deleteById(id)     - 根据主键删除
    // count()            - 统计数量

    // 如果以后你想根据学号查询，只需要写这一行，JPA 会自动生成 SQL：
    Student findByStudentId(String studentId);
}