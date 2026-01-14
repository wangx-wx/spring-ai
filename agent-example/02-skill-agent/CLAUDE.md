# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring AI Alibaba Agent Framework 的技能代理（Skill Agent）项目。项目实现了一个可扩展的技能系统，允许 AI 代理通过读取 SKILL.md 文件来获取特定领域的知识和工作流程指导。

## 构建和运行

```bash
# 构建项目（从项目根目录）
mvn clean package -pl 02-skill-agent -am

# 运行应用
mvn spring-boot:run -pl 02-skill-agent

# 单独构建当前模块
cd 02-skill-agent && mvn clean package
```

## 技术栈

- Java 17
- Spring Boot
- Spring AI Alibaba (DashScope 集成)
- ReactAgent (来自 spring-ai-alibaba-agent-framework)

## 核心架构

### 技能系统设计

技能系统采用三层设计：
1. **SkillsInterceptor** - 模型拦截器，在每次调用时将可用技能信息注入到系统提示词中
2. **SkillRegistry** - 技能注册中心，使用 ConcurrentHashMap 管理已加载的技能
3. **SkillScanner** - 技能扫描器，从指定目录扫描并解析 SKILL.md 文件

### 技能加载流程

```
SkillsAgent.buildAgent()
  └── SkillsInterceptor.builder()
        └── loadSkills()
              ├── SkillScanner.scan(userSkillsDirectory)
              └── SkillScanner.scan(projectSkillsDirectory)
                    └── loadSkill() - 解析 SKILL.md 的 YAML frontmatter
```

### 技能文件结构

技能位于 `skills/` 目录，每个技能是一个包含 `SKILL.md` 的子目录：

```
skills/
├── arxiv-search/
│   └── SKILL.md
├── skill-creator/
│   └── SKILL.md
└── web-search/
    └── SKILL.md
```

SKILL.md 必须包含 YAML frontmatter：
```yaml
---
name: skill-name
description: 技能描述，用于触发判断
---
```

### ReactAgent 配置

SkillsAgent 构建的 ReactAgent 包含：
- **工具**: ReadFileTool, WriteFileTool, ListFilesTool, ShellTool
- **拦截器**: SkillsInterceptor（注入技能系统提示词）
- **钩子**: ShellToolAgentHook

## API 端点

- `GET /chat?message={message}` - 与技能代理对话

## 关键类

| 类 | 职责 |
|---|---|
| `SkillsAgent` | 构建配置好技能系统的 ReactAgent |
| `SkillsInterceptor` | 拦截模型请求，注入技能信息到系统提示词 |
| `SkillRegistry` | 线程安全的技能注册管理 |
| `SkillScanner` | 扫描目录并解析 SKILL.md |
| `SkillMetadata` | 技能元数据实体（name, description, path, source） |

## 技能目录配置

技能目录路径在 `SkillsAgent` 中硬编码：
```java
private static final String SKILLS_DIR = "agent-example/02-skill-agent/skills";
```

支持两种技能来源：
- **userSkillsDirectory** - 用户级全局技能
- **projectSkillsDirectory** - 项目级技能（同名时覆盖用户级）
