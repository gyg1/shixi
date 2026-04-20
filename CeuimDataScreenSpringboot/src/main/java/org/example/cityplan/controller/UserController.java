package org.example.cityplan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Map;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.User;
import org.example.cityplan.security.RoleType;
import org.example.cityplan.service.AuthService;
import org.example.cityplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    private User getCurrentOperator() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("当前登录用户不存在");
        }
        return currentUser;
    }

    private boolean isAdmin(User user) {
        return user != null && RoleType.ADMIN.getCode().equalsIgnoreCase(user.getRole());
    }

    private boolean isSuperAdmin(User user) {
        return user != null && RoleType.SUPER_ADMIN.getCode().equalsIgnoreCase(user.getRole());
    }

    private void ensureCanManageRole(String targetRole) {
        User operator = getCurrentOperator();
        if (isAdmin(operator) && RoleType.SUPER_ADMIN.getCode().equalsIgnoreCase(targetRole)) {
            throw new RuntimeException("管理员不能分配超级管理员角色");
        }
    }

    private void ensureCanManageUser(Long userId) {
        User operator = getCurrentOperator();
        User targetUser = userService.getById(userId);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if (isAdmin(operator) && isSuperAdmin(targetUser)) {
            throw new RuntimeException("管理员不能操作超级管理员账号");
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<User>>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        IPage<User> page = userService.getUserPage(pageNum, pageSize, keyword, role);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success(user));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            ensureCanManageRole(user.getRole());
            User created = userService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("用户创建成功", created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            ensureCanManageUser(id);
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(ApiResponse.success("用户更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            ensureCanManageUser(id);
            String role = body.get("role");
            ensureCanManageRole(role);
            userService.updateUserRole(id, role);
            return ResponseEntity.ok(ApiResponse.success("角色更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            ensureCanManageUser(id);
            String newPassword = body.get("password");
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        try {
            ensureCanManageUser(id);
            userService.toggleUserStatus(id);
            return ResponseEntity.ok(ApiResponse.success("状态切换成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            ensureCanManageUser(id);
            boolean removed = userService.removeById(id);
            if (removed) {
                return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
            }
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("删除失败"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }
}
