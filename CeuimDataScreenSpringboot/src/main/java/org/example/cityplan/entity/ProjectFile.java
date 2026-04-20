package org.example.cityplan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_project_file")
public class ProjectFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Long uploadedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
