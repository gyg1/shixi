package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 项目 Mapper 接口
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据项目编号查询
     */
    @Select("SELECT * FROM biz_project WHERE project_code = #{code} AND deleted = false")
    Project selectByCode(@Param("code") String code);

    /**
     * 根据项目类型查询
     */
    @Select("SELECT * FROM biz_project WHERE project_type = #{type} AND deleted = false ORDER BY created_at DESC")
    List<Project> selectByType(@Param("type") String type);

    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM biz_project WHERE status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Project> selectByStatus(@Param("status") String status);

    /**
     * 获取项目统计数据
     */
    @Select("SELECT COUNT(*) FROM biz_project WHERE deleted = false")
    int countTotal();

    @Select("SELECT COUNT(*) FROM biz_project WHERE status = 'IN_PROGRESS' AND deleted = false")
    int countInProgress();

    @Select("SELECT COUNT(*) FROM biz_project WHERE status = 'COMPLETED' AND deleted = false")
    int countCompleted();

    /**
     * 单独更新 geometry 字段 (使用 ST_GeomFromGeoJSON 转换)
     */
    @org.apache.ibatis.annotations.Update("UPDATE biz_project SET geometry = ST_GeomFromGeoJSON(#{geojson}) WHERE id = #{id}")
    int updateGeometry(@Param("id") Long id, @Param("geojson") String geojson);

    /**
     * 读取 geometry 为 GeoJSON 字符串
     */
    @Select("SELECT ST_AsGeoJSON(geometry) FROM biz_project WHERE id = #{id} AND deleted = false")
    String selectGeometryById(@Param("id") Long id);

    /**
     * 根据大类查询项目列表
     */
    @Select("SELECT * FROM biz_project WHERE category = #{category} AND deleted = false ORDER BY created_at DESC")
    List<Project> selectByCategory(@Param("category") String category);

    /**
     * 根据大类统计各子类数量和面积
     */
    @Select("SELECT sub_type AS subType, COUNT(*) AS count, COALESCE(SUM(area), 0) AS totalArea " +
            "FROM biz_project WHERE category = #{category} AND deleted = false GROUP BY sub_type")
    List<Map<String, Object>> statisticsByCategory(@Param("category") String category);
}
