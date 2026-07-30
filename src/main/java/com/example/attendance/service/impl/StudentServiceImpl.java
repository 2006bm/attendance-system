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

import com.example.attendance.entity.CourseSelection;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.repository.StudentRepository; // 导入新接口
import com.example.attendance.service.StudentService;
import com.example.attendance.util.ImportResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

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
    @Override
    public Student findByStudentId(String studentId) {
        // 逻辑极其简单：让 Service 去调用你之前写好的 StudentRepository 仓库！
        return studentRepository.findByStudentId(studentId);
    }
    @Override
    public List<Student> getAllStudents() {
        // 调用 JPA 自动查询所有学生
        return studentRepository.findAll();
    }
    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Autowired
    private com.example.attendance.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


//    @Transactional // 开启事务保护
@Override
public ImportResult importStudents(String filePath, String courseId) {
    ImportResult result = new ImportResult();

    try (FileInputStream fis = new FileInputStream(new File(filePath));
         Workbook workbook = WorkbookFactory.create(fis)) {

        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String studentId = getCellValue(row.getCell(0));   // A列 (索引 0): 学号
                String studentName = getCellValue(row.getCell(1)); // B列 (索引 1): 姓名
                String gender = getCellValue(row.getCell(2));      // C列 (索引 2): 性别
                String college = getCellValue(row.getCell(3));     // D列 (索引 3): 学院
                String gradeStr = getCellValue(row.getCell(4));    // E列 (索引 4): 年级
                String major = getCellValue(row.getCell(5));       // F列 (索引 5): 专业
                String className = getCellValue(row.getCell(6));    // G列: 班级

                if (studentId.isEmpty() || studentName.isEmpty()) {
                    throw new RuntimeException("学号和姓名不能为空");
                }

                // 1. 保存学生档案
                Student student = studentRepository.findByStudentId(studentId);
                if (student == null) {
                    student = new Student();
                    student.setStudentId(studentId);
                }
                student.setStudentName(studentName);
                student.setClassName(className);
                student.setGender(gender);
                student.setCollege(college);
                student.setMajor(major);
                if (!gradeStr.isEmpty()) {
                    // 兼容 Excel 的小数字数转换 (比如 2022.0)
                    student.setGrade((int) Double.parseDouble(gradeStr));
                }
                studentRepository.save(student);

                // 2. 👈 【核心优化】：如果他还没选过这门课，才帮他选，防止触发数据库唯一约束冲突！
                if (!courseSelectionRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                    CourseSelection selection = new CourseSelection();
                    selection.setStudentId(studentId);
                    selection.setStudentName(studentName);
                    selection.setCourseId(courseId);
                    selection.setGender(gender);
                    courseSelectionRepository.save(selection);
                }

                // 3. 自动开通登录账号
                if (!userRepository.findByUsername(studentId).isPresent()) {
                    User user = new User();
                    user.setUsername(studentId);
                    user.setRealName(studentName);
                    user.setRole("STUDENT");
                    user.setPassword(passwordEncoder.encode("123456"));
                    userRepository.save(user);
                }

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

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                return java.math.BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id); // JpaRepository 自带的根据主键删除方法
    }

    @Override
    public Page<Student> getStudentsByTeacher(Long teacherId, Pageable pageable) {
        return studentRepository.findByTeacherId(teacherId, pageable);
    }
    @Override
    public long countByTeacherId(Long teacherId){
        return studentRepository.countByTeacherId(teacherId);
    }
}
