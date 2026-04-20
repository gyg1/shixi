package org.example.cityplan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.Property;
import org.example.cityplan.security.CustomUserDetails;
import org.example.cityplan.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 房产信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    /**
     * 分页查询房产信息
     */
    @GetMapping
    public ResponseEntity<ApiResponse<IPage<Property>>> listProperties(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String propertyType) {

        log.info("[房产控制器] 查询列表 - page: {}, size: {}, keyword: {}", pageNum, pageSize, keyword);

        IPage<Property> page = propertyService.getPropertyPage(pageNum, pageSize, keyword, propertyType);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 获取房产详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Property>> getProperty(@PathVariable Long id) {
        log.info("[房产控制器] 查询详情 - id: {}", id);

        Property property = propertyService.getPropertyById(id);
        if (property != null) {
            return ResponseEntity.ok(ApiResponse.success(property));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建房产记录 (管理员及以上)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Property>> createProperty(
            @RequestBody Property property,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[房产控制器] 创建房产 - 名称: {}, 操作人: {}",
                property.getPropertyName(), userDetails.getUsername());

        try {
            Property created = propertyService.createProperty(property);
            log.info("[房产控制器] 房产创建成功 - id: {}", created.getId());
            return ResponseEntity.ok(ApiResponse.success("房产创建成功", created));
        } catch (RuntimeException e) {
            log.error("[房产控制器] 房产创建失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 更新房产记录 (管理员及以上)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Property>> updateProperty(
            @PathVariable Long id,
            @RequestBody Property property,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[房产控制器] 更新房产 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            Property updated = propertyService.updateProperty(id, property);
            log.info("[房产控制器] 房产更新成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("房产更新成功", updated));
        } catch (RuntimeException e) {
            log.error("[房产控制器] 房产更新失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 删除房产记录 (管理员及以上)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[房产控制器] 删除房产 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            propertyService.deleteProperty(id);
            log.info("[房产控制器] 房产删除成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("房产删除成功"));
        } catch (RuntimeException e) {
            log.error("[房产控制器] 房产删除失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 根据类型获取房产
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Property>>> getPropertiesByType(@PathVariable String type) {
        log.info("[房产控制器] 根据类型查询 - type: {}", type);
        List<Property> properties = propertyService.getByType(type);
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    /**
     * 获取所有房产 (用于地图显示)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Property>>> getAllProperties() {
        log.info("[房产控制器] 获取所有房产");
        List<Property> properties = propertyService.getAllProperties();
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    /**
     * 获取房产统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getStatistics() {
        log.info("[房产控制器] 获取统计数据");
        Map<String, Integer> stats = propertyService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
