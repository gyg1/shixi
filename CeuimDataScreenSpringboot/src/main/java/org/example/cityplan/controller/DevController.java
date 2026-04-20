package org.example.cityplan.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.User;
import org.example.cityplan.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 开发调试控制器 (仅用于开发环境)
 * 生产环境应删除此控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/public/dev")
public class DevController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 生成 BCrypt 密码哈希 (开发用)
     */
    @GetMapping("/hash")
    public ApiResponse<String> generateHash(@RequestParam String password) {
        log.info("[DEV] 生成密码哈希: {}", password);
        String hash = passwordEncoder.encode(password);
        log.info("[DEV] 哈希结果: {}", hash);
        return ApiResponse.success(hash);
    }

    /**
     * 重置用户密码 (开发用)
     */
    @GetMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @RequestParam String username,
            @RequestParam String newPassword) {
        log.info("[DEV] 重置密码 - 用户: {}, 新密码: {}", username, newPassword);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.error("[DEV] 用户不存在: {}", username);
            return ApiResponse.badRequest("用户不存在");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userMapper.updateById(user);

        log.info("[DEV] 密码重置成功 - 用户: {}, 新哈希: {}", username, hashedPassword);
        return ApiResponse.success("密码重置成功，新哈希: " + hashedPassword);
    }

    /**
     * 验证密码 (开发用)
     */
    @GetMapping("/verify")
    public ApiResponse<Boolean> verifyPassword(
            @RequestParam String username,
            @RequestParam String password) {
        log.info("[DEV] 验证密码 - 用户: {}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.error("[DEV] 用户不存在: {}", username);
            return ApiResponse.success(false);
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("[DEV] 密码匹配结果: {}", matches);
        log.info("[DEV] 数据库中的哈希: {}", user.getPassword());

        return ApiResponse.success(matches);
    }
}
