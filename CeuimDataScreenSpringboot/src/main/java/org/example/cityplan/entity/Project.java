package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("biz_project")
public class Project {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目编号
     */
    @TableField("project_code")
    private String projectCode;

    /**
     * 项目名称
     */
    @TableField("project_name")
    private String projectName;

    /**
     * 项目类型: 代建/公建/市政/房地产
     */
    @TableField("project_type")
    private String projectType;

    /**
     * 项目状态: PENDING/IN_PROGRESS/COMPLETED/CANCELLED
     */
    @TableField("status")
    private String status;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 项目描述
     */
    @TableField("description")
    private String description;

    /**
     * 项目地址
     */
    @TableField("address")
    private String address;

    /**
     * 空间数据 (GeoJSON 字符串) - 需要用 ST_GeomFromGeoJSON 转换，不走 MyBatis-Plus 自动插入
     */
    @TableField(value = "geometry", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String geometry;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 大类: LAND-土地储备, ENGINEERING-工程项目
     */
    @TableField("category")
    private String category;

    /**
     * 子类: 来自字典
     */
    @TableField("sub_type")
    private String subType;

    /**
     * 动态表单数据 (JSON)
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 面积 (平方米)
     */
    @TableField("area")
    private java.math.BigDecimal area;

    /**
     * 所属区县
     */
    @TableField("district")
    private String district;

    /**
     * 总投资额 (万元)
     */
    @TableField("total_investment")
    private java.math.BigDecimal totalInvestment;

    /**
     * 项目所属二级集团
     */
    @TableField("secondary_group")
    private String secondaryGroup;

    /**
     * 施工单位
     */
    @TableField("construction_unit")
    private String constructionUnit;

    /**
     * 建设单位
     */
    @TableField("development_unit")
    private String developmentUnit;

    /**
     * 资金来源
     */
    @TableField("fund_source")
    private String fundSource;

    /**
     * 手续办理情况
     */
    @TableField("procedures_status")
    private String proceduresStatus;

    /**
     * 竣工手续办理
     */
    @TableField("completion_procedures")
    private String completionProcedures;

    /**
     * 存在问题
     */
    @TableField("existing_problems")
    private String existingProblems;

    /**
     * 截至目前投资 (万元)
     */
    @TableField("current_investment")
    private java.math.BigDecimal currentInvestment;
}
