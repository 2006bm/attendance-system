package com.example.attendance.dto;

public class AttendanceStatisticsDTO {
    private long totalCount;
    private long normalCount;
    private long lateCount;
    private long absentCount;
    private long leaveCount; // 早退
    private double attendanceRate; // 出勤率

    // --- 构造方法 ---
    public AttendanceStatisticsDTO(long totalCount, long normalCount, long lateCount, long absentCount, long leaveCount, double attendanceRate) {
        this.totalCount = totalCount;
        this.normalCount = normalCount;
        this.lateCount = lateCount;
        this.absentCount = absentCount;
        this.leaveCount = leaveCount;
        this.attendanceRate = attendanceRate;
    }

    // --- Getter 和 Setter (必须有) ---
    public long getTotalCount() { return totalCount; }
    public long getNormalCount() { return normalCount; }
    public long getLateCount() { return lateCount; }
    public long getAbsentCount() { return absentCount; }
    public long getLeaveCount() { return leaveCount; }
    public double getAttendanceRate() { return attendanceRate; }
}