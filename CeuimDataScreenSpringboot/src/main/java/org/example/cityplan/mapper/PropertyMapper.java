package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.Property;

import java.util.List;

/**
 * 房产信息 Mapper 接口
 */
@Mapper
public interface PropertyMapper extends BaseMapper<Property> {

    /**
     * 根据房产编号查询
     */
    @Select("SELECT * FROM biz_property WHERE property_code = #{code} AND deleted = false")
    Property selectByCode(@Param("code") String code);

    /**
     * 根据房产类型查询
     */
    @Select("SELECT * FROM biz_property WHERE property_type = #{type} AND deleted = false ORDER BY created_at DESC")
    List<Property> selectByType(@Param("type") String type);

    /**
     * 统计房产数量
     */
    @Select("SELECT COUNT(*) FROM biz_property WHERE deleted = false")
    int countTotal();

    /**
     * 按类型统计
     */
    @Select("SELECT COUNT(*) FROM biz_property WHERE property_type = #{type} AND deleted = false")
    int countByType(@Param("type") String type);
}
