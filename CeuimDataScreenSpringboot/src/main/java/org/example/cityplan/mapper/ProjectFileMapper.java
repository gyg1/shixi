package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.ProjectFile;

import java.util.List;

@Mapper
public interface ProjectFileMapper extends BaseMapper<ProjectFile> {

    @Select("SELECT * FROM biz_project_file WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<ProjectFile> selectByProjectId(@Param("projectId") Long projectId);
}
