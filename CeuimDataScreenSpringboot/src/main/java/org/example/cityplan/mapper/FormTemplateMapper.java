package org.example.cityplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.cityplan.entity.FormTemplate;

@Mapper
public interface FormTemplateMapper extends BaseMapper<FormTemplate> {

    @Select("SELECT * FROM sys_form_template WHERE category = #{category} AND sub_type = #{subType} AND deleted = false LIMIT 1")
    FormTemplate selectByCategoryAndSubType(@Param("category") String category, @Param("subType") String subType);
}
