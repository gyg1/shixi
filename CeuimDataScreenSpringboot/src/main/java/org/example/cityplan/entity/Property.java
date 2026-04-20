package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房产信息实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("biz_property")
public class Property {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房产编号
     */
    @TableField("property_code")
    private String propertyCode;

    /**
     * 房产名称
     */
    @TableField("property_name")
    private String propertyName;

    /**
     * 房产类型: 出售/出租/自持
     */
    @TableField("property_type")
    private String propertyType;

    /**
     * 面积 (平方米)
     */
    @TableField("area")
    private BigDecimal area;

    /**
     * 价格 (万元)
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 状态
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
