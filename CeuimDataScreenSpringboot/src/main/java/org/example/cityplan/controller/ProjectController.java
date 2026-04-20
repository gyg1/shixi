package org.example.cityplan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.Project;
import org.example.cityplan.security.CustomUserDetails;
import org.example.cityplan.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 分页查询项目列表 (所有登录用户可访问)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<IPage<Project>>> listProjects(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) String status) {

        log.info("[项目控制器] 查询项目列表 - page: {}, size: {}, keyword: {}", pageNum, pageSize, keyword);

        IPage<Project> page = projectService.getProjectPage(pageNum, pageSize, keyword, category, projectType, status);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 获取项目详情 (所有登录用户可访问)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProject(@PathVariable Long id) {
        log.info("[项目控制器] 查询项目详情 - id: {}", id);

        Project project = projectService.getProjectById(id);
        if (project != null) {
            return ResponseEntity.ok(ApiResponse.success(project));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建项目 (管理员及以上)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @RequestBody Project project,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[项目控制器] 创建项目 - 名称: {}, 操作人: {}",
                project.getProjectName(), userDetails.getUsername());

        try {
            Project created = projectService.createProject(project, userDetails.getUserId());
            log.info("[项目控制器] 项目创建成功 - id: {}", created.getId());
            return ResponseEntity.ok(ApiResponse.success("项目创建成功", created));
        } catch (RuntimeException e) {
            log.error("[项目控制器] 项目创建失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 更新项目 (管理员及以上)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @PathVariable Long id,
            @RequestBody Project project,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[项目控制器] 更新项目 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            Project updated = projectService.updateProject(id, project);
            log.info("[项目控制器] 项目更新成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("项目更新成功", updated));
        } catch (RuntimeException e) {
            log.error("[项目控制器] 项目更新失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 删除项目 (管理员及以上)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[项目控制器] 删除项目 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            projectService.deleteProject(id);
            log.info("[项目控制器] 项目删除成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("项目删除成功"));
        } catch (RuntimeException e) {
            log.error("[项目控制器] 项目删除失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 根据类型获取项目列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByType(@PathVariable String type) {
        log.info("[项目控制器] 根据类型查询 - type: {}", type);
        List<Project> projects = projectService.getByType(type);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * 根据状态获取项目列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByStatus(@PathVariable String status) {
        log.info("[项目控制器] 根据状态查询 - status: {}", status);
        List<Project> projects = projectService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * 获取所有项目 (地图可视化)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        log.info("[项目控制器] 获取全部项目 (地图)");
        List<Project> projects = projectService.getAllWithGeometry();
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * 获取项目统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getStatistics() {
        log.info("[项目控制器] 获取项目统计");
        Map<String, Integer> stats = projectService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 根据大类获取项目列表 (含 geometry, 用于模块地图展示)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByCategory(@PathVariable String category) {
        log.info("[项目控制器] 根据大类查询 - category: {}", category);
        List<Project> projects = projectService.getByCategoryWithGeometry(category);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * 根据大类统计各子类数量和面积
     */
    @GetMapping("/category/{category}/stats")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategoryStats(@PathVariable String category) {
        log.info("[项目控制器] 根据大类统计 - category: {}", category);
        List<Map<String, Object>> stats = projectService.getStatisticsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
