package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.LandReserve;

import java.util.List;

/**
 * 土地储备 Mapper 接口
 */
@Mapper
public interface LandReserveMapper extends BaseMapper<LandReserve> {

    /**
     * 根据地块编号查询
     */
    @Select("SELECT * FROM biz_land_reserve WHERE land_code = #{code} AND deleted = false")
    LandReserve selectByCode(@Param("code") String code);

    /**
     * 根据土地用途查询
     */
    @Select("SELECT * FROM biz_land_reserve WHERE land_use = #{landUse} AND deleted = false ORDER BY created_at DESC")
    List<LandReserve> selectByLandUse(@Param("landUse") String landUse);

    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM biz_land_reserve WHERE status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<LandReserve> selectByStatus(@Param("status") String status);

    /**
     * 统计土地储备数量
     */
    @Select("SELECT COUNT(*) FROM biz_land_reserve WHERE deleted = false")
    int countTotal();
}
