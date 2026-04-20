package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_form_template")
public class FormTemplate {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_name")
    private String templateName;

    @TableField("category")
    private String category;

    @TableField("sub_type")
    private String subType;

    /**
     * 字段配置 JSON:
     * [{"key":"area","label":"面积","type":"number","required":true,"unit":"㎡"}, ...]
     */
    @TableField("fields")
    private String fields;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
