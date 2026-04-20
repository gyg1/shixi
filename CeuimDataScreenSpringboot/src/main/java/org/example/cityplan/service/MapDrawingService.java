package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.MapDrawing;
import org.example.cityplan.mapper.MapDrawingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地图标绘服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapDrawingService {

    private final MapDrawingMapper mapDrawingMapper;

    /**
     * 获取所有标绘
     */
    public List<MapDrawing> findAll() {
        return mapDrawingMapper.selectList(
                new LambdaQueryWrapper<MapDrawing>()
                        .eq(MapDrawing::getDeleted, false)
                        .orderByDesc(MapDrawing::getCreatedAt));
    }

    /**
     * 根据分类查询
     */
    public List<MapDrawing> findByCategory(String category) {
        return mapDrawingMapper.selectByCategory(category);
    }

    /**
     * 根据业务ID和分类查询
     */
    public List<MapDrawing> findByBusinessIdAndCategory(Long businessId, String category) {
        return mapDrawingMapper.selectByBusinessIdAndCategory(businessId, category);
    }

    /**
     * 根据ID查询
     */
    public MapDrawing findById(Long id) {
        return mapDrawingMapper.selectById(id);
    }

    /**
     * 创建标绘
     */
    @Transactional
    public MapDrawing create(MapDrawing drawing) {
        mapDrawingMapper.insert(drawing);
        log.info("[标绘] 创建成功: id={}, name={}, category={}", drawing.getId(), drawing.getName(), drawing.getCategory());
        return drawing;
    }

    /**
     * 更新标绘
     */
    @Transactional
    public MapDrawing update(Long id, MapDrawing drawing) {
        MapDrawing existing = mapDrawingMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("标绘不存在: " + id);
        }

        drawing.setId(id);
        mapDrawingMapper.updateById(drawing);
        log.info("[标绘] 更新成功: id={}", id);
        return drawing;
    }

    /**
     * 删除标绘 (逻辑删除)
     */
    @Transactional
    public void delete(Long id) {
        MapDrawing existing = mapDrawingMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("标绘不存在: " + id);
        }

        mapDrawingMapper.deleteById(id);
        log.info("[标绘] 删除成功: id={}", id);
    }

    /**
     * 分页查询
     */
    public Page<MapDrawing> findPage(int page, int size, String category) {
        Page<MapDrawing> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MapDrawing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MapDrawing::getDeleted, false);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MapDrawing::getCategory, category);
        }
        wrapper.orderByDesc(MapDrawing::getCreatedAt);
        return mapDrawingMapper.selectPage(pageParam, wrapper);
    }
}
