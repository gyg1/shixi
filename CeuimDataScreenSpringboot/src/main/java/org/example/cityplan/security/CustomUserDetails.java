package org.example.cityplan.security;

import lombok.Getter;
import org.example.cityplan.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义 UserDetails 实现
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据用户角色返回权限
        RoleType roleType = RoleType.fromCode(user.getRole());
        return Collections.singletonList(new SimpleGrantedAuthority(roleType.getAuthority()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled() != null && user.getEnabled();
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 获取用户角色
     */
    public String getRole() {
        return user.getRole();
    }

    /**
     * 获取角色类型
     */
    public RoleType getRoleType() {
        return RoleType.fromCode(user.getRole());
    }

    /**
     * 检查是否是管理员 (ADMIN 或 SUPER_ADMIN)
     */
    public boolean isAdmin() {
        RoleType roleType = getRoleType();
        return roleType == RoleType.ADMIN || roleType == RoleType.SUPER_ADMIN;
    }

    /**
     * 检查是否是超级管理员
     */
    public boolean isSuperAdmin() {
        return getRoleType() == RoleType.SUPER_ADMIN;
    }
}
