package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.User;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = false")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = false")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据角色查询用户列表
     */
    @Select("SELECT * FROM sys_user WHERE role = #{role} AND deleted = false ORDER BY created_at DESC")
    List<User> selectByRole(@Param("role") String role);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = false")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND deleted = false")
    int countByEmail(@Param("email") String email);
}
