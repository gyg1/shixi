package org.example.cityplan.controller;

import jakarta.validation.Valid;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.dto.LoginRequest;
import org.example.cityplan.dto.LoginResponse;
import org.example.cityplan.dto.RegisterRequest;
import org.example.cityplan.entity.User;
import org.example.cityplan.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("用户名或密码错误"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("登录失败: " + e.getMessage()));
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        User user = authService.getCurrentUser();
        if (user != null) {
            return ResponseEntity.ok(ApiResponse.success(user));
        }
        return ResponseEntity.status(401).body(ApiResponse.unauthorized("未登录"));
    }

    /**
     * 验证 Token
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean valid = authService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success(valid));
        }
        return ResponseEntity.ok(ApiResponse.success(false));
    }

    /**
     * 刷新 Token (暂不实现，后续可添加)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken() {
        return ResponseEntity.ok(ApiResponse.success("Token 刷新功能待实现"));
    }
}
