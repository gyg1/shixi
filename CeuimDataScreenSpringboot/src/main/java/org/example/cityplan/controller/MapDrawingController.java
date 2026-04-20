package org.example.cityplan.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.MapDrawing;
import org.example.cityplan.service.MapDrawingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地图标绘控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/map-drawings")
@RequiredArgsConstructor
public class MapDrawingController {

    private final MapDrawingService mapDrawingService;

    /**
     * 获取所有标绘
     */
    @GetMapping
    public ApiResponse<List<MapDrawing>> getAll(@RequestParam(required = false) String category) {
        List<MapDrawing> drawings;
        if (category != null && !category.isEmpty()) {
            drawings = mapDrawingService.findByCategory(category);
        } else {
            drawings = mapDrawingService.findAll();
        }
        return ApiResponse.success(drawings);
    }

    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public ApiResponse<MapDrawing> getById(@PathVariable Long id) {
        MapDrawing drawing = mapDrawingService.findById(id);
        if (drawing == null) {
            return ApiResponse.notFound("标绘不存在");
        }
        return ApiResponse.success(drawing);
    }

    /**
     * 根据业务ID和分类获取
     */
    @GetMapping("/business/{businessId}")
    public ApiResponse<List<MapDrawing>> getByBusinessId(
            @PathVariable Long businessId,
            @RequestParam String category) {
        List<MapDrawing> drawings = mapDrawingService.findByBusinessIdAndCategory(businessId, category);
        return ApiResponse.success(drawings);
    }

    /**
     * 创建标绘
     */
    @PostMapping
    public ApiResponse<MapDrawing> create(@RequestBody MapDrawing drawing) {
        try {
            MapDrawing created = mapDrawingService.create(drawing);
            return ApiResponse.success(created);
        } catch (Exception e) {
            log.error("[标绘] 创建失败", e);
            return ApiResponse.serverError("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新标绘
     */
    @PutMapping("/{id}")
    public ApiResponse<MapDrawing> update(@PathVariable Long id, @RequestBody MapDrawing drawing) {
        try {
            MapDrawing updated = mapDrawingService.update(id, drawing);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            log.error("[标绘] 更新失败", e);
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 删除标绘
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            mapDrawingService.delete(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("[标绘] 删除失败", e);
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 批量创建标绘
     */
    @PostMapping("/batch")
    public ApiResponse<List<MapDrawing>> createBatch(@RequestBody List<MapDrawing> drawings) {
        try {
            for (MapDrawing drawing : drawings) {
                mapDrawingService.create(drawing);
            }
            return ApiResponse.success(drawings);
        } catch (Exception e) {
            log.error("[标绘] 批量创建失败", e);
            return ApiResponse.serverError("批量创建失败: " + e.getMessage());
        }
    }
}
