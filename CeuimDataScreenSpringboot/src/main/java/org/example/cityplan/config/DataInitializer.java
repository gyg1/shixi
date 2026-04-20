package org.example.cityplan.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cityplan.entity.Project;
import org.example.cityplan.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 启动时自动将六大片区 GeoJSON 数据导入数据库
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(ProjectMapper projectMapper, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.projectMapper = projectMapper;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. 确保字典中存在"片区规划"子类（无论片区数据是否已导入）
        ensureDictItem();

        // 2. 修复已有片区数据中 project_type 为中文的情况
        try {
            int fixed = jdbcTemplate.update(
                "UPDATE biz_project SET project_type = 'PIANQUGUIHUA' WHERE category = 'DISTRICT' AND project_type = '片区规划'"
            );
            if (fixed > 0) {
                log.info("[DataInitializer] 已修复 {} 条片区 project_type 为 PIANQUGUIHUA", fixed);
            }
        } catch (Exception e) {
            log.warn("[DataInitializer] 修复 project_type 失败", e);
        }

        // 3. 检查是否已经存在 DISTRICT 类别数据
        long count = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getCategory, "DISTRICT")
        );
        if (count > 0) {
            log.info("[DataInitializer] 六大片区数据已存在 ({} 条)，跳过导入", count);
            return;
        }

        log.info("[DataInitializer] 开始导入六大片区数据...");

        // 从 classpath 读取 GeoJSON 文件
        ClassPathResource resource = new ClassPathResource("六大片区.geojson");
        String geojsonContent;
        try (InputStream is = resource.getInputStream()) {
            geojsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        JsonNode root = objectMapper.readTree(geojsonContent);
        JsonNode features = root.get("features");

        // 每个片区的默认描述
        String[] descriptions = {
                "太平河片区是石家庄北部重点发展区域，依托太平河生态资源，打造集生态宜居、文化创意、商务办公于一体的城市新区。",
                "留营片区位于石家庄西部，是城市更新改造的重点片区，规划建设现代化综合居住社区和商业配套设施。",
                "龙泉湖片区位于石家庄西南方向，以龙泉湖水系为核心，打造生态旅游和高端住宅融合的滨水新城。",
                "和平路片区是石家庄核心城区东部的重要发展板块，依托和平路交通廊道，发展商贸、金融、总部经济。",
                "高铁片区紧邻石家庄高铁站，是城市重点发展区域，致力于打造现代化商务中心和交通枢纽，将成为城市重要的商务、文化和交通中心。",
                "东南三环片区是石家庄东南方向的城市扩展区，规划发展先进制造、科技创新和产城融合示范区。"
        };

        // 每个片区的默认额外数据
        String[] extraDataArr = {
                "{\"投资金额\":\"待定\",\"开发面积\":\"约15平方公里\",\"片区类型\":\"生态宜居\"}",
                "{\"投资金额\":\"待定\",\"开发面积\":\"约8平方公里\",\"片区类型\":\"城市更新\"}",
                "{\"投资金额\":\"待定\",\"开发面积\":\"约20平方公里\",\"片区类型\":\"滨水新城\"}",
                "{\"投资金额\":\"待定\",\"开发面积\":\"约12平方公里\",\"片区类型\":\"商贸金融\"}",
                "{\"投资金额\":\"待定\",\"开发面积\":\"约10平方公里\",\"片区类型\":\"商务枢纽\"}",
                "{\"投资金额\":\"待定\",\"开发面积\":\"约25平方公里\",\"片区类型\":\"产城融合\"}"
        };

        for (int i = 0; i < features.size(); i++) {
            JsonNode feature = features.get(i);
            String name = feature.get("properties").get("name").asText();
            JsonNode geometry = feature.get("geometry");

            // 创建 Project 实体
            Project project = new Project();
            project.setProjectCode("DISTRICT-" + String.format("%03d", i + 1));
            project.setProjectName(name);
            project.setProjectType("PIANQUGUIHUA");
            project.setCategory("DISTRICT");
            project.setStatus("IN_PROGRESS");
            project.setDescription(i < descriptions.length ? descriptions[i] : name + "片区规划");
            project.setExtraData(i < extraDataArr.length ? extraDataArr[i] : "{}");
            project.setDistrict("石家庄市");
            project.setAddress(name);
            project.setStartDate(LocalDate.of(2024, 1, 1));
            project.setEndDate(LocalDate.of(2030, 12, 31));
            project.setCreatedBy(1L);
            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            project.setDeleted(false);

            // 先插入基本信息（geometry交给 updateGeometry 处理）
            projectMapper.insert(project);

            // 再单独更新 geometry
            String geojsonGeometry = objectMapper.writeValueAsString(geometry);
            projectMapper.updateGeometry(project.getId(), geojsonGeometry);

            log.info("[DataInitializer] 已导入片区: {} (id={})", name, project.getId());
        }

        log.info("[DataInitializer] 六大片区数据导入完成！共 {} 条", features.size());
    }

    /**
     * 确保字典表中存在"片区规划"子类
     */
    private void ensureDictItem() {
        try {
            Integer itemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_dict_item WHERE item_code = 'PIANQUGUIHUA' " +
                "AND dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'engineering_sub_type')",
                Integer.class
            );
            if (itemCount == null || itemCount == 0) {
                jdbcTemplate.update(
                    "INSERT INTO sys_dict_item (dict_id, item_code, item_name, sort) " +
                    "VALUES ((SELECT id FROM sys_dict WHERE dict_code = 'engineering_sub_type'), 'PIANQUGUIHUA', '片区规划', 5)"
                );
                log.info("[DataInitializer] 已添加字典项: 片区规划 (PIANQUGUIHUA)");
            }
        } catch (Exception e) {
            log.warn("[DataInitializer] 字典项检查/插入失败", e);
        }
    }
}
