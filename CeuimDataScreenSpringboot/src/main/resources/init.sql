-- =====================================================
-- 三维城市规划项目协同管理系统 - PostgreSQL 初始化脚本
-- =====================================================

-- 创建数据库 (需要手动在psql中执行)
CREATE DATABASE cityplan WITH ENCODING 'UTF8';

-- 连接到 cityplan 数据库后执行以下脚本

-- 启用 PostGIS 扩展
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- =====================================================
-- 用户表 sys_user
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',  -- USER/ADMIN/SUPER_ADMIN
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.role IS '用户角色: USER-普通用户, ADMIN-管理员, SUPER_ADMIN-超级管理员';

-- =====================================================
-- 项目表 biz_project
-- =====================================================
CREATE TABLE IF NOT EXISTS biz_project (
    id BIGSERIAL PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_type VARCHAR(50),  -- 代建/公建/市政/房地产
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/IN_PROGRESS/COMPLETED/CANCELLED
    start_date DATE,
    end_date DATE,
    description TEXT,
    address VARCHAR(500),
    geometry GEOMETRY(GEOMETRY, 4326),
    created_by BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE biz_project IS '项目信息表';
COMMENT ON COLUMN biz_project.geometry IS '项目空间数据 (GeoJSON)';

-- =====================================================
-- 土地储备表 biz_land_reserve
-- =====================================================
CREATE TABLE IF NOT EXISTS biz_land_reserve (
    id BIGSERIAL PRIMARY KEY,
    land_code VARCHAR(50),
    land_name VARCHAR(200) NOT NULL,
    land_use VARCHAR(50),  -- 居住用地/商业用地/工业用地
    area DECIMAL(15,2),
    price DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'IN_STOCK',  -- IN_STOCK/SOLD/RESERVED
    geometry GEOMETRY(MULTIPOLYGON, 4326),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE biz_land_reserve IS '土地储备表';

-- =====================================================
-- 房产信息表 biz_property
-- =====================================================
CREATE TABLE IF NOT EXISTS biz_property (
    id BIGSERIAL PRIMARY KEY,
    property_code VARCHAR(50),
    property_name VARCHAR(200) NOT NULL,
    property_type VARCHAR(50),  -- 出售/出租/自持
    area DECIMAL(15,2),
    price DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    geometry GEOMETRY(GEOMETRY, 4326),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE biz_property IS '房产信息表';

-- =====================================================
-- 危险作业监测点表 biz_danger_point
-- =====================================================
CREATE TABLE IF NOT EXISTS biz_danger_point (
    id BIGSERIAL PRIMARY KEY,
    point_code VARCHAR(50),
    point_name VARCHAR(200) NOT NULL,
    danger_type VARCHAR(50),
    risk_level INTEGER DEFAULT 1,  -- 1-5
    project_id BIGINT REFERENCES biz_project(id),
    location GEOMETRY(POINT, 4326),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE biz_danger_point IS '危险作业监测点表';

-- =====================================================
-- 地图标绘表 map_drawing
-- =====================================================
CREATE TABLE IF NOT EXISTS map_drawing (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    draw_type VARCHAR(20) NOT NULL,  -- POINT/POLYLINE/POLYGON
    category VARCHAR(50) NOT NULL,    -- PROJECT/LAND/PROPERTY/OTHER
    business_id BIGINT,
    geometry TEXT NOT NULL,           -- GeoJSON 字符串
    area DECIMAL(15,2),
    length DECIMAL(15,2),
    description TEXT,
    style TEXT,                       -- JSON 样式配置
    created_by BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE map_drawing IS '地图标绘表';
COMMENT ON COLUMN map_drawing.draw_type IS '标绘类型: POINT-点, POLYLINE-线, POLYGON-面';
COMMENT ON COLUMN map_drawing.category IS '业务分类: PROJECT-项目, LAND-土地, PROPERTY-房产, OTHER-其他';
COMMENT ON COLUMN map_drawing.geometry IS 'GeoJSON 格式的几何数据';

-- =====================================================
-- 系统日志表 sys_log
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES sys_user(id),
    action VARCHAR(100),
    module VARCHAR(50),
    ip_address VARCHAR(50),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_log IS '系统操作日志表';

-- =====================================================
-- 创建空间索引
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_project_geometry ON biz_project USING GIST (geometry);
CREATE INDEX IF NOT EXISTS idx_land_geometry ON biz_land_reserve USING GIST (geometry);
CREATE INDEX IF NOT EXISTS idx_property_geometry ON biz_property USING GIST (geometry);
CREATE INDEX IF NOT EXISTS idx_danger_point_location ON biz_danger_point USING GIST (location);

-- =====================================================
-- 插入初始数据
-- =====================================================

-- 超级管理员 (密码: admin123)
INSERT INTO sys_user (username, password, email, role, real_name, enabled) VALUES 
('superadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'superadmin@cityplan.com', 'SUPER_ADMIN', '超级管理员', TRUE)
ON CONFLICT (username) DO NOTHING;

-- 管理员 (密码: admin123)
INSERT INTO sys_user (username, password, email, role, real_name, enabled) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@cityplan.com', 'ADMIN', '管理员', TRUE)
ON CONFLICT (username) DO NOTHING;

-- 普通用户 (密码: user123)
INSERT INTO sys_user (username, password, email, role, real_name, enabled) VALUES 
('user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'user@cityplan.com', 'USER', '测试用户', TRUE)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 字典类型表 sys_dict
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGSERIAL PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL UNIQUE,
    dict_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE sys_dict IS '系统字典类型表';

-- =====================================================
-- 字典项表 sys_dict_item
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGSERIAL PRIMARY KEY,
    dict_id BIGINT NOT NULL REFERENCES sys_dict(id),
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INTEGER DEFAULT 0,
    color VARCHAR(20),
    status VARCHAR(10) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE sys_dict_item IS '系统字典项表';
COMMENT ON COLUMN sys_dict_item.color IS '地图区域颜色';

-- =====================================================
-- 表单模板表 sys_form_template
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_form_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_type VARCHAR(50) NOT NULL,
    fields JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(category, sub_type)
);
COMMENT ON TABLE sys_form_template IS '项目表单模板表';

-- =====================================================
-- 修改 biz_project 表: 增加分类和动态数据字段
-- =====================================================
ALTER TABLE biz_project ADD COLUMN IF NOT EXISTS category VARCHAR(50);
ALTER TABLE biz_project ADD COLUMN IF NOT EXISTS sub_type VARCHAR(50);
ALTER TABLE biz_project ADD COLUMN IF NOT EXISTS extra_data JSONB DEFAULT '{}';
ALTER TABLE biz_project ADD COLUMN IF NOT EXISTS area DECIMAL(15,2);
ALTER TABLE biz_project ADD COLUMN IF NOT EXISTS district VARCHAR(50);

COMMENT ON COLUMN biz_project.category IS '大类: LAND/ENGINEERING';
COMMENT ON COLUMN biz_project.sub_type IS '子类: 来自字典';
COMMENT ON COLUMN biz_project.extra_data IS '动态表单数据';
COMMENT ON COLUMN biz_project.district IS '所属区县';

-- =====================================================
-- 预置字典数据
-- =====================================================
INSERT INTO sys_dict (dict_code, dict_name, description) VALUES
('project_category', '项目大类', '土地储备和工程项目'),
('land_sub_type', '土地储备子类', '土地储备下的细分类型'),
('engineering_sub_type', '工程项目子类', '工程项目下的细分类型')
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO sys_dict_item (dict_id, item_code, item_name, sort, color) VALUES
((SELECT id FROM sys_dict WHERE dict_code='project_category'), 'LAND', '土地储备', 1, '#FF9800'),
((SELECT id FROM sys_dict WHERE dict_code='project_category'), 'ENGINEERING', '工程项目', 2, '#2196F3');

INSERT INTO sys_dict_item (dict_id, item_code, item_name, sort) VALUES
((SELECT id FROM sys_dict WHERE dict_code='land_sub_type'), 'RESIDENTIAL', '居住用地', 1),
((SELECT id FROM sys_dict WHERE dict_code='land_sub_type'), 'COMMERCIAL', '商业用地', 2),
((SELECT id FROM sys_dict WHERE dict_code='land_sub_type'), 'INDUSTRIAL', '工业用地', 3);

INSERT INTO sys_dict_item (dict_id, item_code, item_name, sort) VALUES
((SELECT id FROM sys_dict WHERE dict_code='engineering_sub_type'), 'DAIJIAN', '代建', 1),
((SELECT id FROM sys_dict WHERE dict_code='engineering_sub_type'), 'GONGJIAN', '公建', 2),
((SELECT id FROM sys_dict WHERE dict_code='engineering_sub_type'), 'SHIZHENG', '市政', 3),
((SELECT id FROM sys_dict WHERE dict_code='engineering_sub_type'), 'FANGDICHAN', '房地产', 4),
((SELECT id FROM sys_dict WHERE dict_code='engineering_sub_type'), 'PIANQUGUIHUA', '片区规划', 5);

-- 项目附件表
CREATE TABLE IF NOT EXISTS biz_project_file (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_size BIGINT,
  file_type VARCHAR(100),
  uploaded_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_project_file_pid ON biz_project_file(project_id);

-- 验证 PostGIS 安装
SELECT PostGIS_Version();