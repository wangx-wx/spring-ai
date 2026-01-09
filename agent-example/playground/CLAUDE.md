# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于Spring AI和Spring Boot的AI聊天playground应用，整合了多个AI模型和工具：
- DashScope (阿里云) 模型：deepseek-r1, deepseek-v3, qwen-plus-2025-01-25
- DeepSeek 模型
- OpenAI 模型 
- MCP (Model Context Protocol) 工具集成
- RAG (检索增强生成) 功能
- 向量存储 (支持PGVector)

## 常用命令

### 构建和运行
```bash
# 构建项目
mvn clean package

# 构建项目(包含前端资源)
mvn clean package -Dnpm.build.skip=false

# 运行应用
mvn spring-boot:run

# 直接运行jar包
java -jar target/app.jar
```

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ClassName

# 跳过测试构建
mvn clean package -DskipTests
```

## 核心架构

### 多模型支持架构
- **ChatClientConfiguration**: 配置不同AI模型的ChatClient (DeepSeek, DashScope)
- **ModelsUtils**: 模型管理和配置读取，支持从`models.yaml`动态加载模型列表
- **ModelEnums**: 定义支持的AI模型枚举

### MCP (Model Context Protocol) 集成
- **mcp-config.yml**: MCP服务器配置文件，定义外部工具服务
- **McpServerUtils**: MCP服务器管理工具类
- **SyncMcpToolCallbackWrapper**: 同步MCP工具调用包装器
- **CustomMcpStdioConfigBeanPostProcessor**: 自定义MCP配置后处理器

### RAG系统架构
- **VectorStoreDelegate**: 向量存储代理，支持多种向量数据库
- **VectorStoreInitializer**: 向量存储初始化器
- **SimpleVectorStoreConfiguration**: 向量存储配置
- 支持PGVector作为向量数据库，可通过`VECTOR_STORE_TYPE`环境变量配置

### 服务层架构
- **BaseService/BaseServiceImpl**: 基础服务层，提供通用功能
- **ChatService/ChatServiceImpl**: 聊天服务，支持普通对话和深度思考模式
- **RagService**: RAG检索增强生成服务
- **ToolsService**: 工具调用服务
- **ImageService/VideoService**: 多媒体处理服务

### 配置和安全
- **AppConfiguration**: 应用主配置
- **XSSFilterConfiguration/XSSFilter**: XSS防护配置
- **GlobalExceptionHandler**: 全局异常处理
- **UserIpAspect**: 用户IP记录切面

## 环境配置

### 必需的环境变量
应用启动时会从`.env`文件读取以下API密钥：
- `DASH_SCOPE_API_KEY`: 阿里云DashScope API密钥
- `DEEPSEEK_API_KEY`: DeepSeek API密钥

### 数据库配置
- 默认使用SQLite作为开发数据库
- 生产环境推荐使用PostgreSQL (特别是需要向量存储功能时)
- 向量存储类型通过`VECTOR_STORE_TYPE=pgvector`环境变量设置

### 应用配置
- 默认端口: 10021
- API文档: http://localhost:10021/swagger-ui.html
- 支持20MB文件上传
- 集成Knife4j API文档增强

## 关键特性

### 聊天功能
- **普通聊天**: `/api/v1/chat` - 支持流式响应
- **深度思考聊天**: `/api/v1/deep-thinking/chat` - 增强推理能力
- 支持chatId进行会话管理和记忆保持

### 工具集成
- MCP工具协议支持外部工具调用
- 默认配置weather工具服务
- 可扩展GitHub等更多MCP服务器

### RAG功能
- 文档向量化和存储
- 基于向量相似度的文档检索
- 支持Markdown文档读取和处理

## 开发注意事项

### MCP服务器管理
- MCP库文件存放在`mcp-libs/`目录
- 部署时会自动复制到`target/mcp-libs/`
- 新增MCP服务器需更新`mcp-config.yml`配置

### 模型配置
- 模型列表在`models.yaml`中配置
- 支持动态添加新模型而无需代码修改
- 每个模型需提供name和description字段

### 数据库迁移
- 项目计划从SQLite迁移到PostgreSQL
- 数据库文件存放在`db/`目录，部署时自动复制