package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.Property;
import org.example.cityplan.mapper.PropertyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房产信息服务
 */
@Slf4j
@Service
public class PropertyService extends ServiceImpl<PropertyMapper, Property> {

    /**
     * 分页查询房产信息
     */
    public IPage<Property> getPropertyPage(int pageNum, int pageSize, String keyword, String propertyType) {
        log.info("[房产服务] 分页查询 - pageNum: {}, pageSize: {}, keyword: {}, type: {}",
                pageNum, pageSize, keyword, propertyType);

        Page<Property> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Property::getPropertyName, keyword)
                    .or()
                    .like(Property::getPropertyCode, keyword));
            log.debug("[房产服务] 关键字搜索: {}", keyword);
        }

        // 类型筛选
        if (StringUtils.hasText(propertyType)) {
            wrapper.eq(Property::getPropertyType, propertyType);
            log.debug("[房产服务] 类型筛选: {}", propertyType);
        }

        wrapper.orderByDesc(Property::getCreatedAt);

        IPage<Property> result = this.page(page, wrapper);
        log.info("[房产服务] 查询结果 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 获取房产详情
     */
    public Property getPropertyById(Long id) {
        log.info("[房产服务] 查询详情 - id: {}", id);
        Property property = this.getById(id);
        if (property == null) {
            log.warn("[房产服务] 房产不存在 - id: {}", id);
        }
        return property;
    }

    /**
     * 创建房产记录
     */
    @Transactional
    public Property createProperty(Property property) {
        log.info("[房产服务] 创建房产 - 名称: {}, 类型: {}", property.getPropertyName(), property.getPropertyType());

        // 检查编号是否存在
        if (StringUtils.hasText(property.getPropertyCode())) {
            Property existing = baseMapper.selectByCode(property.getPropertyCode());
            if (existing != null) {
                log.error("[房产服务] 房产编号已存在 - code: {}", property.getPropertyCode());
                throw new RuntimeException("房产编号已存在: " + property.getPropertyCode());
            }
        }

        if (property.getStatus() == null) {
            property.setStatus("AVAILABLE");
        }
        property.setDeleted(false);

        this.save(property);
        log.info("[房产服务] 房产创建成功 - id: {}, code: {}", property.getId(), property.getPropertyCode());

        return property;
    }

    /**
     * 更新房产记录
     */
    @Transactional
    public Property updateProperty(Long id, Property property) {
        log.info("[房产服务] 更新房产 - id: {}", id);

        Property existing = this.getById(id);
        if (existing == null) {
            log.error("[房产服务] 房产不存在 - id: {}", id);
            throw new RuntimeException("房产不存在");
        }

        // 更新字段
        if (StringUtils.hasText(property.getPropertyName())) {
            existing.setPropertyName(property.getPropertyName());
        }
        if (StringUtils.hasText(property.getPropertyType())) {
            existing.setPropertyType(property.getPropertyType());
        }
        if (property.getArea() != null) {
            existing.setArea(property.getArea());
        }
        if (property.getPrice() != null) {
            existing.setPrice(property.getPrice());
        }
        if (StringUtils.hasText(property.getStatus())) {
            existing.setStatus(property.getStatus());
        }
        if (StringUtils.hasText(property.getGeometry())) {
            existing.setGeometry(property.getGeometry());
            log.debug("[房产服务] 更新空间数据");
        }

        this.updateById(existing);
        log.info("[房产服务] 房产更新成功 - id: {}, 名称: {}", id, existing.getPropertyName());

        return existing;
    }

    /**
     * 删除房产记录
     */
    @Transactional
    public void deleteProperty(Long id) {
        log.info("[房产服务] 删除房产 - id: {}", id);

        Property property = this.getById(id);
        if (property == null) {
            log.error("[房产服务] 房产不存在 - id: {}", id);
            throw new RuntimeException("房产不存在");
        }

        this.removeById(id);
        log.info("[房产服务] 房产删除成功 - id: {}, 名称: {}", id, property.getPropertyName());
    }

    /**
     * 根据类型获取房产列表
     */
    public List<Property> getByType(String type) {
        log.info("[房产服务] 根据类型查询 - type: {}", type);
        return baseMapper.selectByType(type);
    }

    /**
     * 获取所有房产 (用于地图显示)
     */
    public List<Property> getAllProperties() {
        log.info("[房产服务] 获取所有房产");
        List<Property> properties = this.list();
        log.debug("[房产服务] 查询到 {} 个房产", properties.size());
        return properties;
    }

    /**
     * 获取房产统计数据
     */
    public Map<String, Integer> getStatistics() {
        log.info("[房产服务] 获取统计数据");

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", baseMapper.countTotal());
        stats.put("sale", baseMapper.countByType("出售"));
        stats.put("rent", baseMapper.countByType("出租"));
        stats.put("holding", baseMapper.countByType("自持"));

        log.debug("[房产服务] 统计数据 - 总数: {}, 出售: {}, 出租: {}, 自持: {}",
                stats.get("total"), stats.get("sale"), stats.get("rent"), stats.get("holding"));

        return stats;
    }
}
