package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 危险作业申报实体
 */
@Data
@TableName("biz_dangerous_work")
public class DangerousWork {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 申报编号（自动生成 DW-YYYYMMDD-XXX） */
    @TableField("work_code")
    private String workCode;

    /** 关联片区项目ID */
    @TableField("district_project_id")
    private Long districtProjectId;

    /** 所属片区名称 */
    @TableField("district_name")
    private String districtName;

    /** 作业类型: 高空作业/受限空间作业/危化品储存/起重作业/明火作业 */
    @TableField("work_type")
    private String workType;

    /** 作业名称 */
    @TableField("work_name")
    private String workName;

    /** 风险等级: 重大风险/较大风险/一般风险 */
    @TableField("risk_level")
    private String riskLevel;

    /** 申请人 */
    @TableField("applicant")
    private String applicant;

    /** 负责人 */
    @TableField("responsible_person")
    private String responsiblePerson;

    /** 项目实施主体 */
    @TableField("implementor")
    private String implementor;

    /** 位置描述 */
    @TableField("location_desc")
    private String locationDesc;

    /** 经度 */
    @TableField("lng")
    private Double lng;

    /** 纬度 */
    @TableField("lat")
    private Double lat;

    /** 计划开始时间 */
    @TableField("plan_start_time")
    private LocalDateTime planStartTime;

    /** 计划结束时间 */
    @TableField("plan_end_time")
    private LocalDateTime planEndTime;

    /** 安全措施（逗号分隔） */
    @TableField("safety_measures")
    private String safetyMeasures;

    /** 作业描述 */
    @TableField("work_description")
    private String workDescription;

    /** 状态: IN_PROGRESS/COMPLETED/CANCELLED */
    @TableField("status")
    private String status;

    /** 监控点编号（自动生成 MONITOR_XXX） */
    @TableField("monitor_code")
    private String monitorCode;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
