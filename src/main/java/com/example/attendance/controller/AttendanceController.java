package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private com.example.attendance.repository.StudentRepository studentRepository;
    @Autowired
    private com.example.attendance.repository.CourseSelectionRepository courseSelectionRepository;
    // 1. 教师发起签到并生成动态验证码（大屏幕二维码的内容就是指向这个地址）
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/start-session")
    @ResponseBody
    public Result<String> startSession(@RequestParam String courseId, @RequestParam(defaultValue = "5") int minutes) {
        String code = attendanceService.startSignSession(courseId, minutes);
        return Result.success(code); // 返回 4 位动态验证码给教师端前端
    }

    // 2. 学生手机扫描二维码后直接打卡接口
    @GetMapping("/quick-checkin")
    @jakarta.transaction.Transactional
    public String quickCheckIn(@RequestParam String courseId,
                               @RequestParam String code,
                               jakarta.servlet.http.HttpSession session, // 👈 引入 HttpSession
                               Authentication authentication, // 👈 引入 Authentication 用于判断登录状态
                               RedirectAttributes redirectAttributes) {

        // 1. 👈 核心优化：判断当前扫描二维码的学生是否已经登录
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            // 👈 改为：直接通过 URL 传参重定向，将参数安全送达登录页
            return "redirect:/login?targetCourseId=" + courseId + "&targetCode=" + code;
        }

        // ==================== 以下为已登录逻辑（与您之前的逻辑完全一致） ====================
        String studentId = getCurrentStudentId();
        User user = userRepository.findByUsername(studentId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "扫码失败：未找到该账号信息！");
            return "redirect:/dashboard";
        }

        // 自动建档
        Student student = studentRepository.findByStudentId(studentId);
        if (student == null) {
            student = new Student();
            student.setStudentId(studentId);
            student.setStudentName(user.getRealName());
            student.setGender("男");
            studentRepository.save(student);
        }

        // 自动选课
        boolean isEnrolled = courseSelectionRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!isEnrolled) {
            com.example.attendance.entity.CourseSelection selection = new com.example.attendance.entity.CourseSelection();
            selection.setStudentId(studentId);
            selection.setStudentName(student.getStudentName());
            selection.setCourseId(courseId);
            selection.setSelectTime(LocalDateTime.now());
            courseSelectionRepository.save(selection);
        }

        // 验证验证码
        boolean isValid = attendanceService.verifySignCode(courseId, code);
        if (!isValid) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：签到码错误或已过期！");
            return "redirect:/attendance/checkIn";
        }

        try {
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(student.getStudentName());
            attendance.setCourseId(courseId);
            attendanceService.signIn(attendance);

            redirectAttributes.addFlashAttribute("success", "🟢 扫码成功！已自动为您【注册本课档案】并【签到成功】。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }

        return "redirect:/attendance/checkIn";
    }
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
                          @RequestParam(required = false) String signCode, // 👈 核心修改：接收前端学生手动输入的签到口令
                          @RequestParam String action,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        String studentId = getCurrentStudentId();
        Student student = studentService.findByStudentId(studentId);

        if (student == null) {
            model.addAttribute("errorMsg", "打卡失败：您当前登录的账号 [" + studentId + "] 不是学生，无法打卡！");
            model.addAttribute("courses", courseService.findByStudentId(studentId));
            return "attendance-check-in";
        }

        try {
            if ("signin".equals(action)) {
                // 👈 核心修改：当执行“签到”操作时，必须强校验输入的验证码
                boolean isValid = attendanceService.verifySignCode(courseId, signCode);
                if (!isValid) {
                    throw new RuntimeException("签到失败：您输入的签到口令不正确，或该口令已过期！");
                }

                // 执行原签到逻辑
                Attendance attendance = new Attendance();
                attendance.setStudentId(studentId);
                attendance.setStudentName(student.getStudentName());
                attendance.setCourseId(courseId);
                attendance.setStatus("NORMAL");

                attendanceService.signIn(attendance);
                redirectAttributes.addFlashAttribute("success", "🟢 签到成功！已为您记录。");

            } else if ("signout".equals(action)) {
                // 签退不需要口令
                attendanceService.signOut(studentId, courseId);
                redirectAttributes.addFlashAttribute("success", "🟡 签退成功！系统已自动校验是否早退。");
            }

            return "redirect:/attendance/checkIn?success=true";

        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("courses", courseService.findByStudentId(studentId));
            return "attendance-check-in";
        }
    }

    @GetMapping("/checkIn")
    public String checkInPage(
            @RequestParam(required = false) String courseId, // 👈 1. 核心修改：接收扫码传过来的课程 ID！
            Model model,
            Authentication authentication) {

        String studentId = authentication.getName();
        List<Course> courses = courseService.findByStudentId(studentId);

        model.addAttribute("courses", courses);
        // 👈 2. 核心修改：把扫码带过来的课程ID送去前端
        model.addAttribute("selectedCourseId", courseId);

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
    @Autowired
    private com.example.attendance.repository.UserRepository userRepository;
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String quickRange,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        // 1. 👈 【核心修复】精准获取当前登录人的三种不同角色
        boolean isStudent = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));
        boolean isTeacher = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isStudent", isStudent);
        // 解析日期 (保持不变)
        LocalDate parsedStart = null;
        LocalDate parsedEnd = null;
        if ("today".equals(quickRange)) {
            parsedStart = LocalDate.now();
            parsedEnd = LocalDate.now();
        } else if ("week".equals(quickRange)) {
            parsedStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            parsedEnd = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        } else if ("month".equals(quickRange)) {
            parsedStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            parsedEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        } else {
            if (startDate != null && !startDate.isEmpty()) parsedStart = LocalDate.parse(startDate);
            if (endDate != null && !endDate.isEmpty()) parsedEnd = LocalDate.parse(endDate);
        }

        final LocalDate finalStart = parsedStart;
        final LocalDate finalEnd = parsedEnd;

        // 构建动态 SQL 规则 (JPA Specification)
        Specification<Attendance> spec = Specification.where((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 2. 👈 【核心修复】：根据三端身份，执行完全不同的权限隔离隔离查询！
            if (isStudent) {
                // A. 学生：只能看自己的考勤记录
                predicates.add(cb.equal(root.get("studentId"), username));
            } else if (isTeacher) {
                // B. 老师：只能看自己教的课程的考勤
                List<Course> myCourses = courseService.findByTeacherId(user.getId());
                List<String> courseIds = myCourses.stream().map(Course::getCourseId).collect(Collectors.toList());
                if (!courseIds.isEmpty()) {
                    predicates.add(root.get("courseId").in(courseIds));
                } else {
                    predicates.add(cb.disjunction()); // 没教课的老师什么都查不到
                }
            }
            // C. 管理员 (ADMIN)：不需要在 predicates 中锁定任何 courseIds！因为他默认可以查全校所有课！

            // --- 公共筛选条件 ---
            if (courseId != null && !courseId.isEmpty()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
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

        // 3. 👈 【核心修复】：给下拉框塞入不同的课程选项
        if (isStudent) {
            model.addAttribute("courses", courseService.findByStudentId(username));
        } else if (isTeacher) {
            model.addAttribute("courses", courseService.findByTeacherId(user.getId())); // 老师看自己教的课
        } else if (isAdmin) {
            model.addAttribute("courses", courseService.getAllCourses()); // 管理员看全校所有的课程！
        }

        model.addAttribute("records", records.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", records.getTotalPages());
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", status);
        model.addAttribute("quickRange", quickRange);
        model.addAttribute("startDate", finalStart);
        model.addAttribute("endDate", finalEnd);

        return "attendance-list";
    }

    // 3. 新增数据导出功能：直接生成 Excel 可读的 CSV 文件下载
    @GetMapping("/export")
    public void exportAttendance(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseId,
            Authentication authentication, // 👈 1. 引入 Spring Security 认证对象
            HttpServletResponse response) throws IOException {

        if (authentication == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录，无法导出数据！");
            return;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        boolean isStudent = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

        // 2. 👈 动态构建多角色导出过滤条件
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isStudent) {
                // A. 学生：只能导出自己的考勤
                predicates.add(cb.equal(root.get("studentId"), username));
            } else {
                // B. 老师：导出自己教的课程的所有学生的考勤记录
                List<Course> myCourses = courseService.findByTeacherId(user.getId());
                // 提取出老师教的所有课程 ID 列表
                List<String> courseIds = myCourses.stream()
                        .map(Course::getCourseId)
                        .collect(Collectors.toList());

                if (!courseIds.isEmpty()) {
                    predicates.add(root.get("courseId").in(courseIds));
                } else {
                    // 如果老师一门课都没教，强制返回空结果
                    predicates.add(cb.disjunction());
                }
            }

            // --- 公共筛选条件 ---
            // 课程筛选
            if (courseId != null && !courseId.isEmpty()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Attendance> list = attendanceService.getFilteredList(spec);

        // 3. 设置浏览器下载响应头
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");

        // 4. 写入 UTF-8 BOM，防止 Excel 打开中文时乱码
        PrintWriter writer = response.getWriter();
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
        writer.close();
    }

    @Value("${file.upload.path}")
    private String uploadPath;


    // 1. 跳转到导入页面
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/import")
    public String importPage(Model model) {
        // 查出系统里所有的课程，塞给下拉框
        model.addAttribute("courses", courseService.getAllCourses()); // 确保你 CourseService 里有查询所有课程的方法，没有的话写个 courseRepository.findAll()
        return "attendance-import";
    }

    // 2. 处理上传请求
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("courseId") String courseId, // 👈 接收前端选择的目标课程ID
                             RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择文件！");
            return "redirect:/attendance/import";
        }

        try {
            File destDir = new File(uploadPath);
            if (!destDir.exists()) destDir.mkdirs();

            String filename = file.getOriginalFilename();
            String filePath = uploadPath + filename;
            file.transferTo(new File(filePath));

            // 👈 核心修改：调用学生服务，进行多表大联动导入！
            ImportResult result = studentService.importStudents(filePath, courseId);

            redirectAttributes.addFlashAttribute("success",
                    "学生花名册导入并自动选课成功！成功创建账号：" + result.getSuccessCount() + " 个，失败：" + result.getFailCount() + " 条。");
            redirectAttributes.addFlashAttribute("errors", result.getErrors());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }
}