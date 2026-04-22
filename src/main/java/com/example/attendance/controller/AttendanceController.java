package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.Result;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 提交考勤记录
    @PostMapping("/signin")
    public Result<Attendance> signIn(@RequestBody Attendance attendance) {
        Attendance saved = attendanceService.signIn(attendance);
        return Result.success(saved);
    }

    // 查询某个学生的考勤历史
    @GetMapping("/history/{studentId}")
    public Result<List<Attendance>> getHistory(@PathVariable String studentId) {
        List<Attendance> history = attendanceService.getStudentHistory(studentId);
        return Result.success(history);
    }

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/search")
    public Result<Page<Attendance>> searchAttendance(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("checkInTime").descending());

        // 开始构建 Specification (火锅底料)
        Specification<Attendance> spec = (root, query, cb) -> {
            // 1. 准备一个篮子放 Predicate (过滤条件)
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 2. 动态判断并添加条件
            // 按学号查询
            if (studentId != null && !studentId.isEmpty()) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            // 按状态查询
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 按日期范围查询 (支持可选：如果只有开始时间，就查 >=；如果都有，查 between)
            if (startTime != null && endTime != null) {
                predicates.add(cb.between(root.get("checkInTime"), startTime, endTime));
            } else if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), startTime));
            } else if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), endTime));
            }

            // 3. 将篮子里的所有条件用 AND 连接起来
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 4. 执行查询
        Page<Attendance> result = attendanceRepository.findAll(spec, pageable);
        return Result.success(result);
    }
//    @GetMapping("/page")
//    public Result<Page<Attendance>> getAttendancePage(

//            // defaultValue 是字符串，page 默认从 0 开始（第一页是 0）
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "5") int size,
//            @RequestParam(required = false) String studentId,
//            // 知识运用：通过 @RequestParam 接收前端传来的“排序字段”和“方向”
//            @RequestParam(defaultValue = "checkInTime") String sortField, // 默认按考勤日期
//            @RequestParam(defaultValue = "desc") String direction) {
//
//        // 知识运用：将字符串 "asc"/"desc" 转换为 JPA 认识的 Sort.Direction 枚举
//        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
//                ? Sort.Direction.ASC : Sort.Direction.DESC;
//
//        // 知识运用：创建 Sort 对象。Sort.by(方向, 字段)
//        Sort sort = Sort.by(sortDirection, sortField);
//
//        // 知识运用：将创建好的排序规则 sort 塞进分页对象 Pageable 中
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<Attendance> result;
//        if (studentId != null && !studentId.isEmpty()) {
//            result = attendanceRepository.findByStudentId(studentId, pageable);
//        } else {
//            result = attendanceRepository.findAll(pageable);
//        }
//
//        return Result.success(result);
//    }


//            @RequestParam(required = false) String studentId) {
//
//        // 1. 创建分页请求对象（还可以顺便加个排序，按签到时间倒序）
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Attendance> result;
//
//        // 2. 逻辑判断：是查询某个人的分页，还是查全部人的分页
//
//        if (studentId != null && !studentId.isEmpty()) {
//            result = attendanceRepository.findByStudentId(studentId, pageable);
//        } else {
//            result = attendanceRepository.findAll(pageable);
//        }
//
//        // 3. 返回统一格式
//        return Result.success(result);
//    }
}