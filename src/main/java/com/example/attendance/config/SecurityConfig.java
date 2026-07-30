package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 👈 1. 新增：【核心黑科技】动态登录成功处理器
    private AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            // 👈 核心修改：直接从请求中获取表单提交上来的隐藏域参数
            String targetCourseId = request.getParameter("targetCourseId");
            String targetCode = request.getParameter("targetCode");

            if (targetCourseId != null && !targetCourseId.isEmpty()
                    && targetCode != null && !targetCode.isEmpty()) {

                // 👈 完美重定向：直接跳转回扫码打卡接口
                response.sendRedirect("/attendance/quick-checkin?courseId=" + targetCourseId + "&code=" + targetCode);
                return;
            }

            // 无打卡参数时，走正常跳转
            if (roles.contains("ROLE_STUDENT")) {
                response.sendRedirect("/attendance/checkIn");
            } else {
                response.sendRedirect("/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 👈 在放行列表里加入 "/attendance/quick-checkin"
                        .requestMatchers("/", "/dashboard", "/login", "/register", "/user/register", "/attendance/quick-checkin", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/student/**").hasAnyRole("TEACHER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // 👈 3. 核心修改：使用我们上面定义的动态处理器替换掉死板的单路径跳转！
                        .successHandler(successHandler())
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}