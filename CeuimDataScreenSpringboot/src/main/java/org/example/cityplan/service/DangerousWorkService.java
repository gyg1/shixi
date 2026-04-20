package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.DangerousWork;
import org.example.cityplan.mapper.DangerousWorkMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 危险作业申报服务
 */
@Slf4j
@Service
public class DangerousWorkService extends ServiceImpl<DangerousWorkMapper, DangerousWork> {

    private static final AtomicInteger MONITOR_SEQ = new AtomicInteger(100);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 申报危险作业（提交即上线，无需审批）
     */
    @Transactional
    public DangerousWork apply(DangerousWork work) {
        // 自动生成申报编号
        String dateStr = LocalDateTime.now().format(DATE_FMT);
        String workCode = "DW-" + dateStr + "-" + String.format("%03d", (int)(Math.random() * 900) + 100);
        work.setWorkCode(workCode);

        // 自动生成监控点编号
        int seq = MONITOR_SEQ.incrementAndGet();
        work.setMonitorCode("MONITOR_" + seq);

        // 默认状态为进行中
        work.setStatus("IN_PROGRESS");

        this.save(work);
        log.info("[危险作业] 新申报 workCode={}, monitorCode={}", workCode, work.getMonitorCode());
        return work;
    }

    /**
     * 获取地图监控点列表（进行中）
     */
    public List<DangerousWork> getMonitors() {
        return baseMapper.selectMonitors();
    }

    /**
     * 查询列表（支持按状态/作业类型筛选）
     */
    public List<DangerousWork> list(String status, String workType) {
        LambdaQueryWrapper<DangerousWork> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(DangerousWork::getStatus, status);
        }
        if (StringUtils.hasText(workType)) {
            wrapper.eq(DangerousWork::getWorkType, workType);
        }
        wrapper.orderByDesc(DangerousWork::getCreatedAt);
        return this.list(wrapper);
    }

    /**
     * 更新状态
     */
    @Transactional
    public void updateStatus(Long id, String status) {
        DangerousWork work = this.getById(id);
        if (work != null) {
            work.setStatus(status);
            this.updateById(work);
            log.info("[危险作业] 更新状态 id={}, status={}", id, status);
        }
    }

    /**
     * 大屏统计数据
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 基础计数
        stats.put("total", baseMapper.countTotal());
        stats.put("inProgress", baseMapper.countInProgress());
        stats.put("completed", baseMapper.countCompleted());
        stats.put("cancelled", baseMapper.countCancelled());

        // 按类型+状态统计（用于柱状图）
        List<Map<String, Object>> typeStats = baseMapper.countByTypeAndStatus();
        stats.put("typeStats", typeStats);

        // 按风险等级统计（用于甜甜圈图）
        List<Map<String, Object>> riskStats = baseMapper.countByRiskLevel();
        stats.put("riskStats", riskStats);

        // 月度趋势（最近6个月）
        List<Map<String, Object>> trend = baseMapper.monthlyTrend();
        stats.put("monthlyTrend", trend);

        // 最近10条安全记录
        List<DangerousWork> recent = baseMapper.selectRecent(10);
        stats.put("recentRecords", recent);

        log.info("[危险作业] 获取统计数据 total={}", stats.get("total"));
        return stats;
    }
}
