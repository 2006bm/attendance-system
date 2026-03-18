package com.example.attendance.common;

public class Result<T> {
    private Integer code;    // 状态码：200代表成功
    private String message;  // 提示信息："操作成功"
    private T data;          // 具体的数据：比如你的 Student 对象

    // 构造方法
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态快捷方法：成功时调用
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // --- 下面是必须的 Getter 和 Setter，没有它们前端拿不到数据 ---
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
