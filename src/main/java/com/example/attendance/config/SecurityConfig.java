package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 1. 定义加密器：以后所有的密码对比都用 BCrypt 算法
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 定义过滤链：谁能进哪个门，都在这里定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 教学阶段先禁用 CSRF，方便 Postman 测试
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/user/register", "/register","/css/**", "/js/**").permitAll() // 登录和注册不需要门票
                        .anyRequest().authenticated()                       // 其余所有接口都要登录后才能看
                )
                .formLogin(form -> form
                        .loginPage("/login")             // 告诉系统，登录页去哪里找
                        .loginProcessingUrl("/login")   // 告诉系统，登录请求往哪里发
                        .defaultSuccessUrl("/dashboard") // 登录成功后去哪
                        .permitAll()) // 使用系统自带的登录页面
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}