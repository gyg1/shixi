# shixi

三维城市规划协同管理系统项目仓库。

## 项目结构

```text
F:\project\shixi
├─ CeuimDataScreen                # 前端，Vue 3 + TypeScript + Vite + Cesium
├─ CeuimDataScreenSpringboot      # 后端，Spring Boot 3 + Spring Security + MyBatis-Plus + PostgreSQL/PostGIS
├─ uploads                        # 本地上传目录（已忽略，不进仓库）
├─ 系统现状文档.md
├─ 接口清单文档.md
├─ 数据表结构与关系文档.md
├─ 页面-接口-数据表映射文档.md
└─ 模块迭代路线与待办清单.md
```

## 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Pinia
- Vue Router
- Element Plus
- Cesium
- ECharts

### 后端

- Java 17
- Spring Boot 3.2
- Spring Security
- JWT
- MyBatis-Plus
- PostgreSQL / PostGIS

## 本地启动

### 1. 启动后端

工作目录：`CeuimDataScreenSpringboot`

```bash
mvn spring-boot:run
```

默认后端地址：

- `http://localhost:8080`

### 2. 启动前端

工作目录：`CeuimDataScreen`

```bash
npm install
npm run dev
```

默认前端地址：

- `http://localhost:5173`

## 常用命令

### 前端

```bash
cd CeuimDataScreen
npm run dev
npm run build
npm run type-check
```

### 后端

```bash
cd CeuimDataScreenSpringboot
mvn compile
mvn test
mvn spring-boot:run
```

## 当前仓库约定

- 主分支：`main`
- 提交规范：使用 Conventional Commits，详见 [CONTRIBUTING.md](./CONTRIBUTING.md)
- 提交模板：仓库根目录 [.gitmessage.txt](./.gitmessage.txt)
- PR 模板：[`/.github/PULL_REQUEST_TEMPLATE.md`](./.github/PULL_REQUEST_TEMPLATE.md)

## 分支策略

建议采用以下分支模型：

- `main`：受保护分支，只接受 Pull Request 合并
- `feature/*`：功能开发
- `fix/*`：缺陷修复
- `refactor/*`：重构
- `docs/*`：文档更新

## GitHub 分支保护建议

当前仓库已经补了基础 CI 工作流，后续可在 GitHub 仓库设置中对 `main` 启用以下规则：

1. Require a pull request before merging
2. Require approvals: `1`
3. Dismiss stale pull request approvals when new commits are pushed
4. Require conversation resolution before merging
5. Require status checks to pass before merging
6. Required status checks:
   - `backend-compile`
7. Restrict direct pushes to `main`

说明：

- 目前前端 `UserMapView.vue` 仍在收口过程中，暂不建议把前端 `type-check` 直接设成必需检查，否则会持续阻塞合并。
- 等前端类型问题收完，再把前端检查补进 CI 并设为 required check。

## 现阶段状态

- 项目已经完成首次 GitHub 入库
- 前后端基础结构已齐
- 管理端用户管理页已接真实接口
- 项目管理列表已补分页、筛选、详情、附件查看
- `UserMapView.vue` 已开始第一轮拆分，但仍需继续模块化和类型收口

## 相关文档

- [系统现状文档](./系统现状文档.md)
- [接口清单文档](./接口清单文档.md)
- [数据表结构与关系文档](./数据表结构与关系文档.md)
- [页面-接口-数据表映射文档](./页面-接口-数据表映射文档.md)
- [模块迭代路线与待办清单](./模块迭代路线与待办清单.md)
