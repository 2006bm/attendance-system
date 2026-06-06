package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository; // 👈 记得注入这个
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.ImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Attendance signIn(Attendance attendance) {
        // 1. 获取课程信息
        Course course = courseRepository.findById(attendance.getCourseId())
                .orElseThrow(() -> new RuntimeException("未找到该课程！"));

        LocalTime nowTime = LocalTime.now(); // 当前时刻
        LocalTime classStart = course.getStartTime(); // 上课时间 (如 08:00)

        // 2. 时间限制校验：只能在 [上课前15分钟, 上课后30分钟] 内打卡
        LocalTime allowedStart = classStart.minusMinutes(15); // 07:45
        LocalTime allowedEnd = classStart.plusMinutes(30);   // 08:30

        if (nowTime.isBefore(allowedStart) || nowTime.isAfter(allowedEnd)) {
            // 抛出异常，阻止打卡，并带上失败提示
            throw new RuntimeException("打卡失败：当前不在打卡时间范围内（允许时间："
                    + allowedStart + " ~ " + allowedEnd + "）");
        }

        // 3. 自动判定迟到：超过上课时刻，设为迟到
        if (nowTime.isAfter(classStart)) {
            attendance.setStatus("LATE"); // 迟到
        } else {
            attendance.setStatus("NORMAL"); // 正常出勤
        }

        attendance.setCheckInTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance signOut(String studentId, String courseId) {
        // 1. 找到该学生今天这门课的签到记录
        // (为了简单，这里模拟找最新的一条记录。真实开发需要按日期和学生、课程定位)
        List<Attendance> records = attendanceRepository.findByStudentId(studentId);
        Attendance attendance = records.stream()
                .filter(r -> r.getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("签退失败：未找到今日签到记录！"));

        Course course = courseRepository.findById(courseId).orElseThrow();
        LocalTime nowTime = LocalTime.now();
        LocalTime classEnd = course.getEndTime(); // 下课时间 (如 09:30)

        // 2. 判定早退：如果当前时间早于下课时间，标记为早退
        if (nowTime.isBefore(classEnd)) {
            attendance.setStatus("EARLY_LEAVE"); // 早退
        }

        // 3. 保存更改
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getStudentHistory(String studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public Page<Attendance> getFilteredPage(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    @Override
    public List<Attendance> getFilteredList(Specification<Attendance> spec) {
        return attendanceRepository.findAll(spec);
    }

    @Override
    public ImportResult importFromExcel(String filePath) {
        ImportResult result = new ImportResult();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentNo = getCellValue(row.getCell(1));   // B列: 学号
                    String studentName = getCellValue(row.getCell(2)); // C列: 姓名
                    String courseId = getCellValue(row.getCell(3));    // D列: 课程ID
                    String checkInTimeStr = getCellValue(row.getCell(4)); // E列: 时间
                    String status = getCellValue(row.getCell(7));      // H列: 状态

                    if (studentNo.isEmpty() || courseId.isEmpty() || checkInTimeStr.isEmpty()) {
                        throw new RuntimeException("关键数据（学号/课程ID/打卡时间）不能为空");
                    }

                    Attendance attendance = new Attendance();
                    attendance.setStudentId(studentNo);
                    attendance.setStudentName(studentName.isEmpty() ? "未知学生" : studentName);
                    attendance.setCourseId(courseId);
                    attendance.setStatus(status.isEmpty() ? "NORMAL" : status);
                    attendance.setCheckInTime(parseDateTime(checkInTimeStr));

                    // 👈 【核心修复】使用 Double 解析再转 Byte，100% 兼容 Excel 的 5.0 格式！
                    String seatRowStr = getCellValue(row.getCell(5));
                    String seatColStr = getCellValue(row.getCell(6));
                    if (!seatRowStr.isEmpty()) {
                        attendance.setSeatRow((byte) Double.parseDouble(seatRowStr));
                    }
                    if (!seatColStr.isEmpty()) {
                        attendance.setSeatCol((byte) Double.parseDouble(seatColStr));
                    }

                    attendanceRepository.save(attendance);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.incrementFail();
                    result.addError("第 " + (i + 1) + " 行导入失败，原因：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取 Excel 文件失败：" + e.getMessage());
        }
        return result;
    }

    // 👈 【任务二要求】多日期格式兼容解析器
    private LocalDateTime parseDateTime(String dateStr) {
        // 定义可能出现的各种时间格式
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss", // 兼容带 T 的 ISO 格式
                "yyyy-MM-dd"             // 如果只有日期，默认设为当天 00:00:00
        };

        for (String pattern : patterns) {
            try {
                if (pattern.equals("yyyy-MM-dd")) {
                    return java.time.LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern)).atStartOfDay();
                }
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
                // 如果解析失败，尝试下一种格式
            }
        }
        throw new RuntimeException("无法解析的日期时间格式: [" + dateStr + "]");
    }
    // 辅助方法：读取单元格并一律转换为 String 格式
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期格式
                    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                // 防止科学计数法
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}