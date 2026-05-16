//package com.example.attendance.service.impl;
//
//import com.example.attendance.dao.StudentDao;
//import com.example.attendance.entity.Student;
//import com.example.attendance.service.StudentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class StudentServiceImpl implements StudentService {
//    @Autowired
//    private StudentDao studentDao;
//
//    @Override
//    public String createStudent(Student student) {
//        if(student.getStudentName()==null || student.getStudentName().isEmpty()) {
//            throw new RuntimeException("姓名不能为空");
//        }
//        studentDao.insert(student);
//        return "创建成功";
//    }
//    @Override
//    public Student getStudentById(String studentId) {
//        return studentDao.findById(studentId);
//    }
//}

package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository; // 导入新接口
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository; // 注入 JPA 仓库

    @Override
    public String createStudent(Student student) {
        // 1. 简单的业务校验
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new RuntimeException("学号不能为空！");
        }

        // 2. 调用 JPA 的 save 方法。这就是“自动驾驶”！
        // 它会自动生成 INSERT INTO student_info ... 的 SQL 并执行
        studentRepository.save(student);

        return "学生信息已成功存入 PostgreSQL 数据库！";
    }

    @Override
    public Student getStudentById(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Override
    public void save(Student student) {
        studentRepository.save(student);
    }

    @Override
    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable); // JPA自带findAll支持分页
    }
    @Override
    public Student findById(Long id) {
        // 假设你的 repository 是 studentRepository
        return studentRepository.findById(id).orElse(null);
    }
}
