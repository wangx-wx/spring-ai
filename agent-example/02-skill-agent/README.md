# Skill Agent

基于 Spring AI Alibaba Agent Framework 的可扩展技能代理系统。

## 项目简介

Skill Agent 是一个智能代理框架，通过"技能"机制扩展 AI 代理的能力。技能是以 Markdown 文件（SKILL.md）形式定义的指令文档，包含特定领域的知识、工作流程和工具使用指南。

**核心理念**：技能不是工具，而是教会 AI 如何使用现有工具完成特定任务的"说明书"。

## 技术栈

- Java 17
- Spring Boot
- Spring AI Alibaba (DashScope)
- ReactAgent Framework

## 快速开始

### 1. 配置环境变量

创建 `.env` 文件或设置环境变量：

```properties
DASHSCOPE_API_KEY=your_api_key
```

### 2. 配置技能目录

在 `application.yml` 中配置：

```yaml
skill:
  agent:
    skills-dir: skills  # 技能目录路径
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

### 4. 调用接口

```bash
curl "http://localhost:8080/chat?message=搜索关于transformer的论文"
```

## 技能系统

### 技能目录结构

```
skills/
├── arxiv-search/
│   ├── SKILL.md          # 技能定义文件（必需）
│   └── arxiv_search.py   # 辅助脚本（可选）
├── web-search/
│   └── SKILL.md
└── skill-creator/
    └── SKILL.md
```

### SKILL.md 格式

每个技能必须包含 YAML frontmatter：

```markdown
---
name: skill-name
description: 技能描述，用于判断何时触发此技能
---

# 技能标题

## 使用场景
描述何时使用此技能...

## 使用方法
具体的操作步骤...
```

### 内置技能

| 技能名称 | 说明 |
|---------|------|
| `arxiv-search` | 搜索 arXiv 学术论文 |
| `web-search` | 结构化 Web 研究工作流 |
| `skill-creator` | 创建新技能的指南 |

## 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    ChatController                        │
│                      /chat API                           │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     ReactAgent                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  ChatModel  │  │    Tools    │  │ SkillsInterceptor│  │
│  │ (DashScope) │  │ (File/Shell)│  │  (注入技能提示词) │  │
│  └─────────────┘  └─────────────┘  └────────┬────────┘  │
└─────────────────────────────────────────────┼───────────┘
                                              │
                          ┌───────────────────┴───────────┐
                          ▼                               ▼
                 ┌─────────────────┐           ┌─────────────────┐
                 │  SkillRegistry  │◄──────────│  SkillScanner   │
                 │   (技能注册中心)  │           │  (扫描SKILL.md) │
                 └─────────────────┘           └─────────────────┘
```

### 工作流程

1. **启动时**：SkillScanner 扫描技能目录，解析所有 SKILL.md 的 frontmatter
2. **请求时**：SkillsInterceptor 将可用技能列表注入到系统提示词
3. **执行时**：AI 根据用户请求匹配技能，读取 SKILL.md 获取详细指令，使用内置工具执行任务

## 内置工具

ReactAgent 配置了以下工具供技能使用：

| 工具 | 功能 |
|------|------|
| `read_file` | 读取文件内容 |
| `write_file` | 写入文件 |
| `list_files` | 列出目录文件 |
| `shell` | 执行 Shell 命令 |

## 创建新技能

1. 在 `skills/` 下创建新目录
2. 创建 `SKILL.md` 文件，包含 frontmatter 和使用说明
3. （可选）添加辅助脚本或资源文件
4. 重启应用或调用热加载接口

详细指南请参考 `skills/skill-creator/SKILL.md`。

## 配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `skill.agent.skills-dir` | 技能目录路径 | `skills` |

## License

MIT
