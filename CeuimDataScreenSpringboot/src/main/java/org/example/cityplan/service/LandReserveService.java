package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.LandReserve;
import org.example.cityplan.mapper.LandReserveMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 土地储备服务
 */
@Slf4j
@Service
public class LandReserveService extends ServiceImpl<LandReserveMapper, LandReserve> {

    /**
     * 分页查询土地储备
     */
    public IPage<LandReserve> getLandPage(int pageNum, int pageSize, String keyword, String landUse, String status) {
        log.info("[土地储备服务] 分页查询 - pageNum: {}, pageSize: {}, keyword: {}, landUse: {}, status: {}",
                pageNum, pageSize, keyword, landUse, status);

        Page<LandReserve> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LandReserve> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(LandReserve::getLandName, keyword)
                    .or()
                    .like(LandReserve::getLandCode, keyword));
            log.debug("[土地储备服务] 关键字搜索: {}", keyword);
        }

        // 用途筛选
        if (StringUtils.hasText(landUse)) {
            wrapper.eq(LandReserve::getLandUse, landUse);
            log.debug("[土地储备服务] 用途筛选: {}", landUse);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(LandReserve::getStatus, status);
            log.debug("[土地储备服务] 状态筛选: {}", status);
        }

        wrapper.orderByDesc(LandReserve::getCreatedAt);

        IPage<LandReserve> result = this.page(page, wrapper);
        log.info("[土地储备服务] 查询结果 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 获取土地详情
     */
    public LandReserve getLandById(Long id) {
        log.info("[土地储备服务] 查询详情 - id: {}", id);
        LandReserve land = this.getById(id);
        if (land == null) {
            log.warn("[土地储备服务] 地块不存在 - id: {}", id);
        }
        return land;
    }

    /**
     * 创建土地储备记录
     */
    @Transactional
    public LandReserve createLand(LandReserve land) {
        log.info("[土地储备服务] 创建地块 - 名称: {}, 用途: {}", land.getLandName(), land.getLandUse());

        // 检查编号是否存在
        if (StringUtils.hasText(land.getLandCode())) {
            LandReserve existing = baseMapper.selectByCode(land.getLandCode());
            if (existing != null) {
                log.error("[土地储备服务] 地块编号已存在 - code: {}", land.getLandCode());
                throw new RuntimeException("地块编号已存在: " + land.getLandCode());
            }
        }

        if (land.getStatus() == null) {
            land.setStatus("IN_STOCK");
        }
        land.setDeleted(false);

        this.save(land);
        log.info("[土地储备服务] 地块创建成功 - id: {}, code: {}", land.getId(), land.getLandCode());

        return land;
    }

    /**
     * 更新土地储备记录
     */
    @Transactional
    public LandReserve updateLand(Long id, LandReserve land) {
        log.info("[土地储备服务] 更新地块 - id: {}", id);

        LandReserve existing = this.getById(id);
        if (existing == null) {
            log.error("[土地储备服务] 地块不存在 - id: {}", id);
            throw new RuntimeException("地块不存在");
        }

        // 更新字段
        if (StringUtils.hasText(land.getLandName())) {
            existing.setLandName(land.getLandName());
        }
        if (StringUtils.hasText(land.getLandUse())) {
            existing.setLandUse(land.getLandUse());
        }
        if (land.getArea() != null) {
            existing.setArea(land.getArea());
        }
        if (land.getPrice() != null) {
            existing.setPrice(land.getPrice());
        }
        if (StringUtils.hasText(land.getStatus())) {
            existing.setStatus(land.getStatus());
        }
        if (StringUtils.hasText(land.getGeometry())) {
            existing.setGeometry(land.getGeometry());
            log.debug("[土地储备服务] 更新空间数据");
        }

        this.updateById(existing);
        log.info("[土地储备服务] 地块更新成功 - id: {}, 名称: {}", id, existing.getLandName());

        return existing;
    }

    /**
     * 删除土地储备记录
     */
    @Transactional
    public void deleteLand(Long id) {
        log.info("[土地储备服务] 删除地块 - id: {}", id);

        LandReserve land = this.getById(id);
        if (land == null) {
            log.error("[土地储备服务] 地块不存在 - id: {}", id);
            throw new RuntimeException("地块不存在");
        }

        this.removeById(id);
        log.info("[土地储备服务] 地块删除成功 - id: {}, 名称: {}", id, land.getLandName());
    }

    /**
     * 获取所有地块 (用于地图显示)
     */
    public List<LandReserve> getAllLands() {
        log.info("[土地储备服务] 获取所有地块");
        List<LandReserve> lands = this.list();
        log.debug("[土地储备服务] 查询到 {} 个地块", lands.size());
        return lands;
    }

    /**
     * 根据用途获取地块列表
     */
    public List<LandReserve> getByLandUse(String landUse) {
        log.info("[土地储备服务] 根据用途查询 - landUse: {}", landUse);
        return baseMapper.selectByLandUse(landUse);
    }

    /**
     * 统计土地储备数量
     */
    public int countTotal() {
        log.info("[土地储备服务] 统计总数");
        return baseMapper.countTotal();
    }
}
