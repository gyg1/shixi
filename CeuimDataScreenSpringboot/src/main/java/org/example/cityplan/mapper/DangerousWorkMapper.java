package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.cityplan.entity.DangerousWork;

import java.util.List;
import java.util.Map;

/**
 * 危险作业申报 Mapper
 */
@Mapper
public interface DangerousWorkMapper extends BaseMapper<DangerousWork> {

    /** 获取地图监控点列表（仅进行中） */
    @Select("SELECT id, work_code, monitor_code, work_type, work_name, risk_level, " +
            "responsible_person, implementor, location_desc, lng, lat, " +
            "plan_start_time, plan_end_time, safety_measures, work_description, " +
            "status, district_name, created_at, updated_at " +
            "FROM biz_dangerous_work WHERE status = 'IN_PROGRESS' AND deleted = false " +
            "ORDER BY created_at DESC")
    List<DangerousWork> selectMonitors();

    /** 总数 */
    @Select("SELECT COUNT(*) FROM biz_dangerous_work WHERE deleted = false")
    int countTotal();

    /** 进行中数量 */
    @Select("SELECT COUNT(*) FROM biz_dangerous_work WHERE status = 'IN_PROGRESS' AND deleted = false")
    int countInProgress();

    /** 已完成数量 */
    @Select("SELECT COUNT(*) FROM biz_dangerous_work WHERE status = 'COMPLETED' AND deleted = false")
    int countCompleted();

    /** 已取消数量 */
    @Select("SELECT COUNT(*) FROM biz_dangerous_work WHERE status = 'CANCELLED' AND deleted = false")
    int countCancelled();

    /** 按作业类型统计（进行中/已完成） */
    @Select("SELECT work_type AS workType, status, COUNT(*) AS cnt " +
            "FROM biz_dangerous_work WHERE deleted = false " +
            "GROUP BY work_type, status ORDER BY work_type")
    List<Map<String, Object>> countByTypeAndStatus();

    /** 按风险等级统计 */
    @Select("SELECT risk_level AS riskLevel, COUNT(*) AS cnt " +
            "FROM biz_dangerous_work WHERE deleted = false " +
            "GROUP BY risk_level")
    List<Map<String, Object>> countByRiskLevel();

    /** 按月统计（最近6个月申报数和完成数） */
    @Select("SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed, " +
            "SUM(CASE WHEN risk_level = '重大风险' THEN 1 ELSE 0 END) AS highRisk " +
            "FROM biz_dangerous_work WHERE deleted = false " +
            "AND created_at >= NOW() - INTERVAL '6 months' " +
            "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
            "ORDER BY month")
    List<Map<String, Object>> monthlyTrend();

    /** 最近N条安全记录 */
    @Select("SELECT id, work_code, work_name, work_type, district_name, status, created_at, updated_at " +
            "FROM biz_dangerous_work WHERE deleted = false " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<DangerousWork> selectRecent(@Param("limit") int limit);

    /** 按workCode查重 */
    @Select("SELECT COUNT(*) FROM biz_dangerous_work WHERE work_code = #{workCode}")
    int countByWorkCode(@Param("workCode") String workCode);
}
