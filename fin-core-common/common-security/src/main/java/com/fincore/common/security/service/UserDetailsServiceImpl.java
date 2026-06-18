package com.fincore.common.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fincore.common.base.entity.User;
import com.fincore.common.base.mapper.UserMapper;
import com.fincore.common.security.jwt.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user: {}", username);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getStatus, 1));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在或已禁用: " + username);
        }
        log.info("User found: id={}, password length={}", user.getId(), 
                 user.getPassword() != null ? user.getPassword().length() : 0);
        // TODO: Phase 7 - load roles/permissions from t_user_role, t_role_permission
        return new JwtUserDetails(user.getId(), user.getUsername(), user.getPassword(),
                List.of("ADMIN"),
                List.of("order:create", "order:view", "order:edit", "order:delete",
                        "payment:view", "payment:create", "payment:approve",
                        "fund:view", "fund:create", "fund:reconcile",
                        "report:view", "report:export"));
    }
}
