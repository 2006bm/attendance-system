package com.example.attendance.service.impl;

import com.example.attendance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("正在尝试登录的用户是：{}", username);
        // 1. 去数据库找这个用户
        com.example.attendance.entity.User myUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));
        System.out.println("数据库中找到的用户密码是：" + myUser.getPassword());
        // 2. 关键！把我们数据库的 User 转换成 Spring Security 认识的 UserDetails
        // 注意：这里为了防止重名冲突，我直接用了 Spring 自带 User 类的全路径
        return org.springframework.security.core.userdetails.User.withUsername(myUser.getUsername())
                .password(myUser.getPassword()) // 数据库存的必须是密文！

                .roles(myUser.getRole())        // 角色，如 TEACHER, ADMIN
                .build();

    }

}