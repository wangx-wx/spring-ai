<!--
Sync Impact Report:
- Version: 1.0.0 (initial constitution)
- Ratified: 2026-01-09
- Last Amended: 2026-01-09

Added Principles:
  - I. Component Architecture (Vue 3 Composition API)
  - II. Type Safety (TypeScript Strict Mode)
  - III. Progressive Enhancement (MVP First)
  - IV. State Management (Pinia Centralization)
  - V. API Integration (Axios Interceptors)
  - VI. User Experience (Element Plus Consistency)
  - VII. Code Quality (ESLint + Prettier)

Added Sections:
  - Technical Standards
  - Development Workflow

Templates Status:
  - plan-template.md: ✅ Compatible (references constitution check gates)
  - spec-template.md: ✅ Compatible (supports user story priorities)
  - tasks-template.md: ✅ Compatible (aligns with progressive enhancement)

Follow-up TODOs: None
-->

# AI Chat Frontend Constitution

## Core Principles

### I. Component Architecture (Vue 3 Composition API)

- **MUST** 使用 Vue 3 Composition API (`<script setup>`) 而非 Options API
- **MUST** 遵循单一组件职责：每个组件只负责一个明确的功能
- **MUST** 组件可复用性优先：通用 UI 元素必须抽取为独立组件
- **MUST** Props down, Events up：遵循单向数据流原则
- **SHOULD** 使用 `defineProps<T>()` 和 `defineEmits<T>()` 确保类型安全

**Rationale**: Composition API 提供更好的代码组织和逻辑复用，单一职责确保组件可测试性和可维护性。

### II. Type Safety (TypeScript Strict Mode)

- **MUST** 项目启用 `strict: true` 模式
- **MUST** 所有组件、函数、变量显式声明类型（禁止使用 `any`）
- **MUST** 使用 interface 定义 Props、Emits、API 响应等数据结构
- **MUST** 使用 `unknown` 而非 `any` 处理动态类型
- **SHOULD** 为复杂业务逻辑定义明确的类型别名

**Rationale**: TypeScript 在编译期捕获错误，类型安全显著降低运行时 bug 和调试成本。

### III. Progressive Enhancement (MVP First)

- **MUST** 优先实现核心聊天功能（消息发送、接收、显示）
- **MUST** 每个功能增量必须独立可用和可测试
- **MUST** 渐进式添加增强功能（流式输出、历史记录、多会话等）
- **SHOULD** 避免过度设计：不需要的功能不要预先实现
- **MUST** 在添加新功能前确保现有功能稳定可用

**Rationale**: MVP 优先确保快速交付可用价值，渐进式增强降低复杂度风险。

### IV. State Management (Pinia Centralization)

- **MUST** 使用 Pinia 进行全局状态管理（禁止 Vuex）
- **MUST** 跨组件共享的状态必须存储在 Store 中
- **MUST** 每个 Store 职责单一（如 chatStore, userStore, settingsStore）
- **MUST** 使用 TypeScript 定义 State、Getters、Actions 类型
- **SHOULD** 组件本地状态使用 `ref<T>`/`reactive<T>`，无需全部放入 Store

**Rationale**: Pinia 提供 Vue 3 原支持，类型友好，单一职责 Store 保证可维护性。

### V. API Integration (Axios Interceptors)

- **MUST** 使用 Axios 进行 HTTP 请求，配合拦截器统一处理
- **MUST** 请求拦截器：统一添加认证 token、请求 ID
- **MUST** 响应拦截器：统一处理错误、数据转换、loading 状态
- **MUST** API 请求函数必须定义明确的请求/响应类型
- **SHOULD** 为流式响应（SSE）提供专用的处理逻辑

**Rationale**: 统一的拦截器确保一致的错误处理和认证逻辑，类型定义提高开发效率。

### VI. User Experience (Element Plus Consistency)

- **MUST** 使用 Element Plus 组件库保持 UI 一致性
- **MUST** 遵循 Element Plus 设计规范（颜色、间距、排版）
- **MUST** 所有用户操作必须有明确的视觉反馈（loading、success、error）
- **MUST** 响应式设计：支持桌面端和移动端适配
- **SHOULD** 为长时间操作提供进度指示

**Rationale**: 一致的 UI 降低学习成本，明确的反馈提升用户体验信心。

### VII. Code Quality (ESLint + Prettier)

- **MUST** 配置 ESLint 规则（推荐 @antfu/eslint-config-vue）
- **MUST** 配置 Prettier 统一代码格式
- **MUST** 提交前自动运行 lint-staged 检查
- **MUST** 组件文件名使用 PascalCase（如 `ChatMessage.vue`）
- **MUST** 工具函数文件名使用 kebab-case（如 `api-client.ts`）
- **SHOULD** 保持函数单一职责，单个函数不超过 50 行

**Rationale**: 统一的代码风格和静态检查显著减少低级错误和协作摩擦。

## Technical Standards

### Technology Stack

- **构建工具**: Vite 5.x
- **框架**: Vue 3.4+ (Composition API)
- **UI 库**: Element Plus 2.x
- **语言**: TypeScript 5.x (strict mode)
- **状态管理**: Pinia 2.x
- **HTTP 客户端**: Axios 1.x
- **路由**: Vue Router 4.x
- **代码规范**: ESLint + Prettier + lint-staged

### Project Structure

```text
src/
├── assets/          # 静态资源（图片、样式）
├── components/      # 通用组件
│   ├── chat/        # 聊天相关组件
│   └── common/      # 通用 UI 组件
├── composables/     # Composition API 复用逻辑
├── stores/          # Pinia stores
├── api/             # API 客户端和类型定义
├── router/          # 路由配置
├── types/           # 全局类型定义
├── utils/           # 工具函数
└── views/           # 页面组件
```

### Performance Guidelines

- **MUST** 路由级别代码分割（使用 `defineAsyncComponent`）
- **MUST** 长列表使用虚拟滚动（如消息历史）
- **SHOULD** 图片使用懒加载
- **SHOULD** 生产环境启用 gzip/brotli 压缩

### Accessibility

- **MUST** 使用语义化 HTML 标签
- **MUST** 为交互元素提供明确的 focus 状态
- **SHOULD** 支持键盘导航

## Development Workflow

### Git Workflow

- **MUST** 使用语义化提交信息（Conventional Commits）
- **MUST** 功能分支命名: `feature/feature-name`, `fix/bug-name`
- **MUST** 提交前通过 ESLint 检查和类型检查

### Code Review Requirements

- **MUST** 所有代码变更需要 Review
- **MUST** Review 检查点：类型安全、组件职责单一、用户体验
- **SHOULD** 保持 PR 小而专注（单个功能或 bug 修复）

### Testing Strategy

- **MUST** 核心业务逻辑编写单元测试（使用 Vitest）
- **SHOULD** 关键用户流程编写 E2E 测试（使用 Playwright）
- **SHOULD** 组件测试覆盖复杂交互逻辑

## Governance

### Amendment Procedure

1. 提出变更建议，说明理由和影响范围
2. 更新宪章版本号（语义化版本）
3. 更新相关模板和文档以保持一致性
4. 通知团队成员变更内容

### Versioning Policy

- **MAJOR**: 移除或重新定义核心原则（需团队讨论）
- **MINOR**: 添加新原则或显著扩展现有原则
- **PATCH**: 文字澄清、错别字修正、非语义性改进

### Compliance Review

- **MUST** 每个 PR 必须符合宪章原则
- **MUST** 定期审查代码库是否符合宪章
- **SHOULD** 发现不一致时及时修正

**Version**: 1.0.0 | **Ratified**: 2026-01-09 | **Last Amended**: 2026-01-09
