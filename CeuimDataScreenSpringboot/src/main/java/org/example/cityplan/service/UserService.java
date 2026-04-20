package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.cityplan.entity.User;
import org.example.cityplan.mapper.UserMapper;
import org.example.cityplan.security.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public IPage<User> getUserPage(int pageNum, int pageSize, String keyword, String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        // 角色筛选
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> result = this.page(page, wrapper);

        // 清除密码
        result.getRecords().forEach(user -> user.setPassword(null));

        return result;
    }

    /**
     * 根据用户名查找用户
     */
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 创建用户 (管理员功能)
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名
        if (baseMapper.countByUsername(user.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱
        if (user.getEmail() != null && baseMapper.countByEmail(user.getEmail()) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setDeleted(false);

        // 默认角色
        if (user.getRole() == null) {
            user.setRole(RoleType.USER.getCode());
        }

        this.save(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(user.getEmail())) {
            existing.setEmail(user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone())) {
            existing.setPhone(user.getPhone());
        }
        if (StringUtils.hasText(user.getRealName())) {
            existing.setRealName(user.getRealName());
        }
        if (StringUtils.hasText(user.getAvatar())) {
            existing.setAvatar(user.getAvatar());
        }
        if (user.getEnabled() != null) {
            existing.setEnabled(user.getEnabled());
        }

        this.updateById(existing);
        existing.setPassword(null);
        return existing;
    }

    /**
     * 更新用户角色 (仅超级管理员)
     */
    @Transactional
    public void updateUserRole(Long id, String role) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证角色有效性
        RoleType.fromCode(role);

        user.setRole(role);
        this.updateById(user);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setEnabled(!user.getEnabled());
        this.updateById(user);
    }

    /**
     * 根据角色获取用户列表
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = baseMapper.selectByRole(role);
        users.forEach(user -> user.setPassword(null));
        return users;
    }
}
