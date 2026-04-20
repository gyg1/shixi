package org.example.cityplan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 三维城市规划项目协同管理系统
 * 基于 Cesium 的 GIS 可视化平台
 */
@SpringBootApplication
@MapperScan("org.example.cityplan.mapper")
public class CityPlanApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityPlanApplication.class, args);
    }
}
