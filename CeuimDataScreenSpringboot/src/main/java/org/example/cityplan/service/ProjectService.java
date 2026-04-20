package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.Project;
import org.example.cityplan.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {

    public IPage<Project> getProjectPage(int pageNum, int pageSize, String keyword, String category, String projectType, String status) {
        log.info("[项目服务] 分页查询 - pageNum: {}, pageSize: {}, keyword: {}, category: {}, type: {}, status: {}",
                pageNum, pageSize, keyword, category, projectType, status);

        Page<Project> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Project::getProjectName, keyword)
                    .or()
                    .like(Project::getProjectCode, keyword)
                    .or()
                    .like(Project::getAddress, keyword));
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(Project::getCategory, category);
        }

        if (StringUtils.hasText(projectType)) {
            wrapper.eq(Project::getProjectType, projectType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Project::getStatus, status);
        }

        wrapper.orderByDesc(Project::getCreatedAt);
        return this.page(page, wrapper);
    }

    public Project getProjectById(Long id) {
        Project project = this.getById(id);
        if (project != null) {
            String geojson = baseMapper.selectGeometryById(id);
            project.setGeometry(geojson);
        }
        return project;
    }

    public Project getByCode(String code) {
        return baseMapper.selectByCode(code);
    }

    @Transactional
    public Project createProject(Project project, Long userId) {
        if (StringUtils.hasText(project.getProjectCode())) {
            Project existing = baseMapper.selectByCode(project.getProjectCode());
            if (existing != null) {
                throw new RuntimeException("项目编号已存在: " + project.getProjectCode());
            }
        }

        String geojson = project.getGeometry();
        project.setCreatedBy(userId);
        if (!StringUtils.hasText(project.getStatus())) {
            project.setStatus("PENDING");
        }
        project.setDeleted(false);

        this.save(project);

        if (StringUtils.hasText(geojson)) {
            baseMapper.updateGeometry(project.getId(), geojson);
            project.setGeometry(geojson);
        }

        return project;
    }

    @Transactional
    public Project updateProject(Long id, Project project) {
        Project existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("项目不存在");
        }

        if (StringUtils.hasText(project.getProjectName())) {
            existing.setProjectName(project.getProjectName());
        }
        if (StringUtils.hasText(project.getProjectType())) {
            existing.setProjectType(project.getProjectType());
        }
        if (StringUtils.hasText(project.getStatus())) {
            existing.setStatus(project.getStatus());
        }
        if (project.getStartDate() != null) {
            existing.setStartDate(project.getStartDate());
        }
        if (project.getEndDate() != null) {
            existing.setEndDate(project.getEndDate());
        }
        if (StringUtils.hasText(project.getDescription())) {
            existing.setDescription(project.getDescription());
        }
        if (StringUtils.hasText(project.getAddress())) {
            existing.setAddress(project.getAddress());
        }
        if (StringUtils.hasText(project.getCategory())) {
            existing.setCategory(project.getCategory());
        }
        if (StringUtils.hasText(project.getSubType())) {
            existing.setSubType(project.getSubType());
        }
        if (StringUtils.hasText(project.getDistrict())) {
            existing.setDistrict(project.getDistrict());
        }
        if (StringUtils.hasText(project.getExtraData())) {
            existing.setExtraData(project.getExtraData());
        }
        if (project.getArea() != null) {
            existing.setArea(project.getArea());
        }
        if (project.getTotalInvestment() != null) {
            existing.setTotalInvestment(project.getTotalInvestment());
        }
        if (StringUtils.hasText(project.getSecondaryGroup())) {
            existing.setSecondaryGroup(project.getSecondaryGroup());
        }
        if (StringUtils.hasText(project.getConstructionUnit())) {
            existing.setConstructionUnit(project.getConstructionUnit());
        }
        if (StringUtils.hasText(project.getDevelopmentUnit())) {
            existing.setDevelopmentUnit(project.getDevelopmentUnit());
        }
        if (StringUtils.hasText(project.getFundSource())) {
            existing.setFundSource(project.getFundSource());
        }
        if (StringUtils.hasText(project.getProceduresStatus())) {
            existing.setProceduresStatus(project.getProceduresStatus());
        }
        if (StringUtils.hasText(project.getCompletionProcedures())) {
            existing.setCompletionProcedures(project.getCompletionProcedures());
        }
        if (StringUtils.hasText(project.getExistingProblems())) {
            existing.setExistingProblems(project.getExistingProblems());
        }
        if (project.getCurrentInvestment() != null) {
            existing.setCurrentInvestment(project.getCurrentInvestment());
        }

        this.updateById(existing);

        if (StringUtils.hasText(project.getGeometry())) {
            baseMapper.updateGeometry(id, project.getGeometry());
            existing.setGeometry(project.getGeometry());
        }

        return existing;
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = this.getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        this.removeById(id);
    }

    public List<Project> getByType(String type) {
        return baseMapper.selectByType(type);
    }

    public List<Project> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    public List<Project> getAllWithGeometry() {
        List<Project> projects = this.list();
        for (Project project : projects) {
            String geojson = baseMapper.selectGeometryById(project.getId());
            project.setGeometry(geojson);
        }
        return projects;
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", baseMapper.countTotal());
        stats.put("inProgress", baseMapper.countInProgress());
        stats.put("completed", baseMapper.countCompleted());
        return stats;
    }

    public List<Project> getByCategoryWithGeometry(String category) {
        List<Project> projects = baseMapper.selectByCategory(category);
        for (Project project : projects) {
            String geojson = baseMapper.selectGeometryById(project.getId());
            project.setGeometry(geojson);
        }
        return projects;
    }

    public List<Map<String, Object>> getStatisticsByCategory(String category) {
        return baseMapper.statisticsByCategory(category);
    }
}
