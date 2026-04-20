package org.example.cityplan.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.DangerousWork;
import org.example.cityplan.service.DangerousWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 危险作业监管控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dangerous-works")
public class DangerousWorkController {

    @Autowired
    private DangerousWorkService dangerousWorkService;

    /**
     * 申报危险作业（提交即上线，无需审批）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DangerousWork>> apply(@RequestBody DangerousWork work) {
        log.info("[危险作业控制器] 新申报 districtName={}, workType={}", work.getDistrictName(), work.getWorkType());
        DangerousWork saved = dangerousWorkService.apply(work);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    /**
     * 查询列表（支持按状态/作业类型筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DangerousWork>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workType) {
        log.info("[危险作业控制器] 查询列表 status={}, workType={}", status, workType);
        return ResponseEntity.ok(ApiResponse.success(dangerousWorkService.list(status, workType)));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DangerousWork>> getById(@PathVariable Long id) {
        DangerousWork work = dangerousWorkService.getById(id);
        if (work == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(work));
    }

    /**
     * 更新状态（完成/取消）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[危险作业控制器] 更新状态 id={}, status={}", id, status);
        dangerousWorkService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
    }

    /**
     * 获取地图监控点（进行中的危险作业，含经纬度）
     */
    @GetMapping("/monitors")
    public ResponseEntity<ApiResponse<List<DangerousWork>>> getMonitors() {
        log.info("[危险作业控制器] 获取地图监控点");
        return ResponseEntity.ok(ApiResponse.success(dangerousWorkService.getMonitors()));
    }

    /**
     * 大屏统计数据（左右两侧面板用）
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        log.info("[危险作业控制器] 获取统计数据");
        return ResponseEntity.ok(ApiResponse.success(dangerousWorkService.getStatistics()));
    }
}
