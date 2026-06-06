package com.example.attendance.config;

import jakarta.servlet.MultipartConfigElement; // 👈 只用这个标准的 Servlet 依赖
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // 直接使用构造方法定义：
        // 参数 1: 临时保存路径 (填空表示默认)
        // 参数 2: 单个文件最大限制 (10MB = 10 * 1024 * 1024 字节)
        // 参数 3: 总请求最大限制 (50MB = 50 * 1024 * 1024 字节)
        // 参数 4: 写入磁盘阈值 (2MB = 2 * 1024 * 1024 字节)
        return new MultipartConfigElement("",
                10 * 1024 * 1024,
                50 * 1024 * 1024,
                2 * 1024 * 1024);
    }
}