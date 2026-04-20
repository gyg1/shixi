package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地图标绘实体
 * 用于存储在地图上绘制的点/线/面要素
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("map_drawing")
public class MapDrawing {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标绘名称
     */
    @TableField("name")
    private String name;

    /**
     * 标绘类型: POINT/POLYLINE/POLYGON
     */
    @TableField("draw_type")
    private String drawType;

    /**
     * 业务分类: PROJECT/LAND/PROPERTY/OTHER
     */
    @TableField("category")
    private String category;

    /**
     * 关联的业务ID (如 project_id, land_id 等)
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * GeoJSON 几何数据
     */
    @TableField("geometry")
    private String geometry;

    /**
     * 面积 (平方米, 仅polygon有效)
     */
    @TableField("area")
    private BigDecimal area;

    /**
     * 长度 (米, 仅polyline有效)
     */
    @TableField("length")
    private BigDecimal length;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 样式配置 (JSON)
     */
    @TableField("style")
    private String style;

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
}
