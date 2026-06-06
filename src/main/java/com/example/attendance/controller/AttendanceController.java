package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.ImportResult;
import com.example.attendance.util.Result;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.File;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
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

    @PostMapping("/checkIn")
    public String checkIn(@RequestParam String courseId,
                          @RequestParam(required = false) String remark,
                          Model model) {
        String studentId = getCurrentStudentId(); // 获取当前登录学号

        Student student = studentService.findByStudentId(studentId);

        if (student == null) {
            model.addAttribute("errorMsg", "打卡失败：您当前登录的账号 [" + studentId + "] 在学生档案库中不存在，无法打卡！");
            List<Course> courses = courseService.findByStudentId(studentId);
            model.addAttribute("courses", courses);
            return "attendance-check-in";
        }
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setStudentName(student.getStudentName());
        attendance.setCourseId(courseId);

        try {
            // 执行打卡
            attendanceService.signIn(attendance);
            // 打卡成功：带上成功提示，重定向回页面
            return "redirect:/attendance/checkIn?success=true";
        } catch (RuntimeException e) {
            // 打卡失败（因时间限制）：捕获异常信息，塞入 Model 返回给前端页面显示！
            model.addAttribute("errorMsg", e.getMessage());

            // 别忘了把课程列表重新塞回去，否则页面报错
            List<Course> courses = courseService.findByStudentId(studentId);
            model.addAttribute("courses", courses);
            return "attendance-check-in"; // 返回原页面展示错误红框
        }
    }

    @GetMapping("/checkIn")
    public String checkInPage(Model model) {
        // 1. 获取当前登录学号
        String studentId = getCurrentStudentId();

        // 2. 查出这个学生选的所有课，塞进下拉框
        List<Course> courses = courseService.findByStudentId(studentId);
        model.addAttribute("courses", courses);

        // 3. 返回你刚才写的 HTML 页面
        return "attendance-check-in";
    }
    private String getCurrentStudentId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseId,      // 1. 新增：课程筛选参数
            @RequestParam(required = false) String quickRange,    // 2. 新增：快速时间参数 (today, week, month)
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        String studentId = getCurrentStudentId();

        // 解析快捷时间 (今天、本周、本月)
        LocalDate parsedStart = null;
        LocalDate parsedEnd = null;

        if ("today".equals(quickRange)) {
            parsedStart = LocalDate.now();
            parsedEnd = LocalDate.now();
        } else if ("week".equals(quickRange)) {
            // 本周一到本周日
            parsedStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            parsedEnd = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        } else if ("month".equals(quickRange)) {
            // 本月第一天到最后一天
            parsedStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            parsedEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {
            // 如果没选快捷时间，就用输入的自定义日期
            if (startDate != null && !startDate.isEmpty()) parsedStart = LocalDate.parse(startDate);
            if (endDate != null && !endDate.isEmpty()) parsedEnd = LocalDate.parse(endDate);
        }

        // 构建动态 SQL 规则 (JPA Specification)
        final LocalDate finalStart = parsedStart;
        final LocalDate finalEnd = parsedEnd;
        Specification<Attendance> spec = Specification.where((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 锁定当前登录学生
            predicates.add(cb.equal(root.get("studentId"), studentId));

            // 课程筛选
            if (courseId != null && !courseId.isEmpty()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 时间段筛选
            if (finalStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), finalStart.atStartOfDay()));
            }
            if (finalEnd != null) {
                predicates.add(cb.lessThan(root.get("checkInTime"), finalEnd.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("checkInTime").descending());
        Page<Attendance> records = attendanceService.getFilteredPage(spec, pageable);

        // 回传参数给 Thymeleaf 页面做表单回显
        model.addAttribute("records", records.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", records.getTotalPages());
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        model.addAttribute("quickRange", quickRange);
        model.addAttribute("startDate", parsedStart);
        model.addAttribute("endDate", parsedEnd);

        // 重新把课程列表塞回去，供下拉框选择
        model.addAttribute("courses", courseService.findByStudentId(studentId));

        return "attendance-list";
    }

    // 3. 新增数据导出功能：直接生成 Excel 可读的 CSV 文件下载
    @GetMapping("/export")
    public void exportAttendance(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseId,
            HttpServletResponse response) throws IOException {

        String studentId = getCurrentStudentId();

        // 1. 构建过滤条件
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("studentId"), studentId));
            if (courseId != null && !courseId.isEmpty()) predicates.add(cb.equal(root.get("courseId"), courseId));
            if (status != null && !status.isEmpty()) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Attendance> list = attendanceService.getFilteredList(spec);

        // 2. 设置浏览器下载响应头 (确保浏览器知道这是个文件)
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");

        // 3. 核心修复：只使用 getWriter()！
        PrintWriter writer = response.getWriter();

        // 4. 写入 UTF-8 的隐藏标识符 \uFEFF，防止 Excel 打开中文时乱码
        writer.write('\uFEFF');

        // 5. 写入表头和数据
        writer.println("学号,学生姓名,课程ID,打卡时间,考勤状态");
        for (Attendance a : list) {
            writer.println(a.getStudentId() + "," +
                    a.getStudentName() + "," +
                    a.getCourseId() + "," +
                    a.getCheckInTime() + "," +
                    a.getStatus());
        }

        writer.flush();
        writer.close(); // 记得关闭流
    }

    @Value("${file.upload.path}")
    private String uploadPath;


    // 1. 跳转到导入页面
    @GetMapping("/import")
    public String importPage() {
        return "attendance-import"; // 对应 templates/attendance-import.html
    }

    // 2. 处理上传请求
    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请先选择一个文件！");
            return "redirect:/attendance/import";
        }

        String filename = file.getOriginalFilename();
        // 验证文件格式是否为 Excel
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "文件格式不正确，只能上传 Excel 文件 (.xls, .xlsx)！");
            return "redirect:/attendance/import";
        }

        ImportResult result = null;
        try {
            // 确保本地上传文件夹存在
            File destDir = new File(uploadPath);
            if (!destDir.exists()) destDir.mkdirs();

            // 保存文件到本地
            String filePath = uploadPath + filename;
            file.transferTo(new File(filePath));

            result = attendanceService.importFromExcel(filePath);

            // 确保这两行都在！
            redirectAttributes.addFlashAttribute("success",
                    "导入完成！成功：" + result.getSuccessCount() + " 条，失败：" + result.getFailCount() + " 条。");
            redirectAttributes.addFlashAttribute("errors", result.getErrors()); // 👈 必须传错误列表

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }
}