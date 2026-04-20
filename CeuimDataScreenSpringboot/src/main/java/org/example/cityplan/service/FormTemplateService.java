package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.cityplan.entity.FormTemplate;
import org.example.cityplan.mapper.FormTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormTemplateService {

    @Autowired
    private FormTemplateMapper mapper;

    public List<FormTemplate> listAll() {
        return mapper.selectList(
                new LambdaQueryWrapper<FormTemplate>().orderByAsc(FormTemplate::getCategory, FormTemplate::getSubType));
    }

    public FormTemplate getById(Long id) {
        return mapper.selectById(id);
    }

    public FormTemplate getByCategoryAndSubType(String category, String subType) {
        return mapper.selectByCategoryAndSubType(category, subType);
    }

    @Transactional
    public FormTemplate create(FormTemplate template) {
        mapper.insert(template);
        return template;
    }

    @Transactional
    public FormTemplate update(Long id, FormTemplate template) {
        template.setId(id);
        mapper.updateById(template);
        return mapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
