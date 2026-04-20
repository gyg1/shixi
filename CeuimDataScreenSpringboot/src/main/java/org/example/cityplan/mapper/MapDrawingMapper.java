package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.MapDrawing;

import java.util.List;

/**
 * 地图标绘 Mapper
 */
@Mapper
public interface MapDrawingMapper extends BaseMapper<MapDrawing> {

    /**
     * 根据分类查询标绘
     */
    @Select("SELECT * FROM map_drawing WHERE category = #{category} AND deleted = 0")
    List<MapDrawing> selectByCategory(@Param("category") String category);

    /**
     * 根据业务ID和分类查询标绘
     */
    @Select("SELECT * FROM map_drawing WHERE business_id = #{businessId} AND category = #{category} AND deleted = 0")
    List<MapDrawing> selectByBusinessIdAndCategory(@Param("businessId") Long businessId,
            @Param("category") String category);
}
