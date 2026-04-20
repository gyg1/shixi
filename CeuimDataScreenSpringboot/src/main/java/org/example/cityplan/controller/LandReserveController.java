package org.example.cityplan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.LandReserve;
import org.example.cityplan.security.CustomUserDetails;
import org.example.cityplan.service.LandReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 土地储备控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lands")
public class LandReserveController {

    @Autowired
    private LandReserveService landReserveService;

    /**
     * 分页查询土地储备 (所有登录用户可访问)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<IPage<LandReserve>>> listLands(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String landUse,
            @RequestParam(required = false) String status) {

        log.info("[土地储备控制器] 查询列表 - page: {}, size: {}, keyword: {}", pageNum, pageSize, keyword);

        IPage<LandReserve> page = landReserveService.getLandPage(pageNum, pageSize, keyword, landUse, status);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 获取土地详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LandReserve>> getLand(@PathVariable Long id) {
        log.info("[土地储备控制器] 查询详情 - id: {}", id);

        LandReserve land = landReserveService.getLandById(id);
        if (land != null) {
            return ResponseEntity.ok(ApiResponse.success(land));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建土地储备记录 (管理员及以上)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LandReserve>> createLand(
            @RequestBody LandReserve land,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[土地储备控制器] 创建地块 - 名称: {}, 操作人: {}",
                land.getLandName(), userDetails.getUsername());

        try {
            LandReserve created = landReserveService.createLand(land);
            log.info("[土地储备控制器] 地块创建成功 - id: {}", created.getId());
            return ResponseEntity.ok(ApiResponse.success("地块创建成功", created));
        } catch (RuntimeException e) {
            log.error("[土地储备控制器] 地块创建失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 更新土地储备记录 (管理员及以上)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LandReserve>> updateLand(
            @PathVariable Long id,
            @RequestBody LandReserve land,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[土地储备控制器] 更新地块 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            LandReserve updated = landReserveService.updateLand(id, land);
            log.info("[土地储备控制器] 地块更新成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("地块更新成功", updated));
        } catch (RuntimeException e) {
            log.error("[土地储备控制器] 地块更新失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 删除土地储备记录 (管理员及以上)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLand(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("[土地储备控制器] 删除地块 - id: {}, 操作人: {}", id, userDetails.getUsername());

        try {
            landReserveService.deleteLand(id);
            log.info("[土地储备控制器] 地块删除成功 - id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("地块删除成功"));
        } catch (RuntimeException e) {
            log.error("[土地储备控制器] 地块删除失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 获取所有地块 (用于地图显示)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LandReserve>>> getAllLands() {
        log.info("[土地储备控制器] 获取所有地块");
        List<LandReserve> lands = landReserveService.getAllLands();
        return ResponseEntity.ok(ApiResponse.success(lands));
    }

    /**
     * 根据用途获取地块
     */
    @GetMapping("/use/{landUse}")
    public ResponseEntity<ApiResponse<List<LandReserve>>> getLandsByUse(@PathVariable String landUse) {
        log.info("[土地储备控制器] 根据用途查询 - landUse: {}", landUse);
        List<LandReserve> lands = landReserveService.getByLandUse(landUse);
        return ResponseEntity.ok(ApiResponse.success(lands));
    }
}
