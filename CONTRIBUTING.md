# Contributing

## 分支命名

请不要直接在 `main` 上开发。

推荐分支命名：

- `feature/xxx`
- `fix/xxx`
- `refactor/xxx`
- `docs/xxx`
- `chore/xxx`

示例：

- `feature/user-management`
- `fix/project-list-pagination`
- `refactor/user-map-header`

## 提交规范

提交信息统一使用 Conventional Commits：

```text
<type>(<scope>): <subject>
```

### type 约定

- `feat`：新功能
- `fix`：缺陷修复
- `refactor`：重构
- `docs`：文档变更
- `style`：纯样式或格式调整
- `test`：测试相关
- `build`：构建流程或依赖调整
- `ci`：CI/CD 配置
- `chore`：杂项维护

### scope 建议

- `frontend`
- `backend`
- `map`
- `project`
- `danger`
- `auth`
- `docs`
- `repo`

### 示例

```text
feat(project): add category filter for project list
fix(map): repair broken strings in user map view
refactor(frontend): extract user map header component
docs(repo): add repository contributing guide
ci(repo): add backend compile workflow
```

## Pull Request 要求

每个 PR 至少说明以下内容：

1. 改了什么
2. 为什么改
3. 影响范围
4. 如何验证
5. 是否涉及数据库、接口或配置变更

## 合并要求

- 优先使用 Pull Request 合并，不直接推送 `main`
- 合并前至少保证后端编译通过
- 涉及前端页面变更时，至少补充截图或录屏说明
- 涉及接口或数据结构变更时，同步更新仓库文档

## 当前阶段特别说明

- `UserMapView.vue` 仍在持续拆分，涉及地图主页面的改动不要一次塞太多逻辑
- 前端类型收口还未完成，地图模块改动前要先确认是否引入新的类型错误
- 涉及上传、路径、Token、数据库连接的改动，优先往环境变量化方向推进
