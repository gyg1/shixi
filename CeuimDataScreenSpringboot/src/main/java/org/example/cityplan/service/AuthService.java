package org.example.cityplan.service;

import org.example.cityplan.dto.LoginRequest;
import org.example.cityplan.dto.LoginResponse;
import org.example.cityplan.dto.RegisterRequest;
import org.example.cityplan.entity.User;
import org.example.cityplan.mapper.UserMapper;
import org.example.cityplan.security.CustomUserDetails;
import org.example.cityplan.security.JwtTokenProvider;
import org.example.cityplan.security.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成 Token
        String token = jwtTokenProvider.generateToken(authentication);

        // 获取用户信息
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration())
                .user(LoginResponse.UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .build())
                .build();
    }

    /**
     * 用户注册 (默认为普通用户)
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && userMapper.countByEmail(request.getEmail()) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setRole(RoleType.USER.getCode()); // 默认普通用户
        user.setEnabled(true);
        user.setDeleted(false);

        userMapper.insert(user);

        // 清除密码后返回
        user.setPassword(null);
        return user;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            user.setPassword(null); // 不返回密码
            return user;
        }
        return null;
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
