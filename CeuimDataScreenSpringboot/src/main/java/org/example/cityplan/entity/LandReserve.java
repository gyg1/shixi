package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 土地储备实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("biz_land_reserve")
public class LandReserve {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地块编号
     */
    @TableField("land_code")
    private String landCode;

    /**
     * 地块名称
     */
    @TableField("land_name")
    private String landName;

    /**
     * 土地用途: 居住用地/商业用地/工业用地
     */
    @TableField("land_use")
    private String landUse;

    /**
     * 面积 (平方米)
     */
    @TableField("area")
    private BigDecimal area;

    /**
     * 价值 (万元)
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 状态: IN_STOCK/SOLD/RESERVED
     */
    @TableField("status")
    private String status;

    /**
     * 空间数据 (GeoJSON)
     */
    @TableField("geometry")
    private String geometry;

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
