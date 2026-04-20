package org.example.cityplan.security;

/**
 * 用户角色枚举
 */
public enum RoleType {
    /**
     * 普通用户 - 仅可查看可视化数据
     */
    USER("USER", "普通用户"),

    /**
     * 管理员 - 项目管理、数据上传
     */
    ADMIN("ADMIN", "管理员"),

    /**
     * 超级管理员 - 全部权限 + 用户管理 + 系统配置
     */
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取 Spring Security 角色名称 (带 ROLE_ 前缀)
     */
    public String getAuthority() {
        return "ROLE_" + this.code;
    }

    /**
     * 根据代码获取角色类型
     */
    public static RoleType fromCode(String code) {
        for (RoleType role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return USER; // 默认返回普通用户
    }

    /**
     * 判断当前角色是否有指定角色的权限
     * SUPER_ADMIN > ADMIN > USER
     */
    public boolean hasPermission(RoleType requiredRole) {
        return this.ordinal() >= requiredRole.ordinal();
    }
}
