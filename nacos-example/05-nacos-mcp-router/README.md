# Nacos MCP Router 示例

基于 Spring AI Alibaba 的 MCP Router 实现，提供 MCP 服务的发现、向量存储和智能路由功能。

## 功能特性

- **语义搜索**：根据任务描述智能发现匹配的 MCP Server
- **向量存储**：使用 Embedding 模型将服务信息向量化，支持语义搜索
- **智能路由**：自动路由请求到合适的 MCP Server
- **服务管理**：添加、初始化和管理 MCP Server 连接
- **工具代理**：代理 LLM 和 MCP Server 之间的工具调用
- **连接诊断**：提供详细的连接状态和问题排查信息

## 核心架构

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST API      │    │  McpRouter       │    │  Vector Store   │
│   Controller    │◄──►│  Service         │◄──►│  (Embedding)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  McpRouterWatcher│
                       │  (定时监控 30s)   │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  Nacos Registry  │
                       └──────────────────┘
```

## 快速开始

### 1. 前置条件

- JDK 17+
- Nacos Server (localhost:8848)
- 已注册的 MCP Server 服务（如 `nacos-mcp-server-sse`）
- DashScope API Key

### 2. 配置环境变量

```bash
export DASH_SCOPE_API_KEY=your-api-key
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:10034` 启动。

## 配置说明

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASH_SCOPE_API_KEY}
    alibaba:
      mcp:
        router:
          enabled: true
          # 需要发现的 MCP Server 服务名称
          service-names:
            - nacos-mcp-server-sse
            - nacos-mcp-server-streamable
          # 服务更新间隔 (毫秒)
          update-interval: 30000
          vector-store:
            # 语义搜索相似度阈值 (0-1)
            similarity-threshold: 0.2
        nacos:
          server-addr: localhost:8848
          namespace: public
          username: nacos
          password: nacos
```

## API 接口

### 服务搜索

```bash
# 语义搜索 MCP Server
GET /api/router/search?query=天气查询&limit=5

# 带关键词搜索
GET /api/router/search?query=数据库操作&keywords=mysql,postgresql
```

### 服务管理

```bash
# 添加并初始化单个服务
POST /api/router/servers/{serviceName}

# 批量初始化服务
POST /api/router/initialize
Content-Type: application/json
["nacos-mcp-server-sse", "nacos-mcp-server-streamable"]
```

### 工具调用

```bash
# 直接调用 MCP Server 工具
POST /api/router/tools/call
Content-Type: application/json
{
  "serviceName": "nacos-mcp-server-sse",
  "toolName": "getCurrentTime",
  "parameters": "{}"
}
```

### AI 智能调用

```bash
# 通过 AI 模型智能选择并调用工具
GET /api/router/chat?message=帮我查询杭州的天气
```

### 服务调试

```bash
# 调试服务连接状态
GET /api/router/debug/{serviceName}
```

## 使用示例

### 1. 初始化服务

```bash
curl -X POST http://localhost:10034/api/router/servers/nacos-mcp-server-sse
```

响应示例：
```json
{
  "success": true,
  "serviceName": "nacos-mcp-server-sse",
  "serviceInfo": {
    "name": "nacos-mcp-server-sse",
    "description": "SSE 协议的 MCP Server",
    "protocol": "mcp-sse",
    "version": "1.0.0"
  },
  "connectionStatus": {
    "connected": true,
    "connectionUrl": "mcp-sse://localhost:10031",
    "message": "连接成功"
  },
  "tools": [
    {
      "name": "getCurrentTime",
      "description": "获取当前时间"
    }
  ]
}
```

### 2. 语义搜索服务

```bash
curl "http://localhost:10034/api/router/search?query=获取当前时间"
```

响应示例：
```json
{
  "success": true,
  "query": "获取当前时间",
  "results": [
    {
      "serviceName": "nacos-mcp-server-sse",
      "description": "SSE 协议的 MCP Server",
      "score": 0.85
    }
  ]
}
```

### 3. 调用工具

```bash
curl -X POST http://localhost:10034/api/router/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "nacos-mcp-server-sse",
    "toolName": "getCurrentTime",
    "parameters": "{}"
  }'
```

### 4. AI 智能调用

```bash
curl "http://localhost:10034/api/router/chat?message=现在几点了"
```

AI 会自动：
1. 搜索合适的 MCP Server
2. 选择正确的工具
3. 调用工具并返回结果

## 与 MCP Gateway 的区别

| 特性 | MCP Router | MCP Gateway |
|------|------------|-------------|
| 主要功能 | 服务发现 + 语义路由 | 服务聚合 + 统一暴露 |
| 向量存储 | 支持 | 不支持 |
| 语义搜索 | 支持 | 不支持 |
| 对外暴露 MCP 协议 | 不支持 | 支持 |
| 使用场景 | 智能选择服务 | 聚合多服务为单一入口 |

## 项目结构

```
05-nacos-mcp-router/
├── pom.xml                              # Maven 依赖
├── README.md                            # 项目文档
└── src/main/
    ├── resources/
    │   └── application.yaml             # 应用配置
    └── java/com/example/wx/
        ├── NacosMcpRouterApplication.java    # 启动类
        └── controller/
            └── RouterController.java         # REST API
```

## 扩展开发

### 自定义向量存储

实现 `McpServerVectorStore` 接口：

```java
@Component
public class CustomVectorStore implements McpServerVectorStore {
    // 实现接口方法
}
```

### 自定义服务发现

实现 `McpServiceDiscovery` 接口：

```java
@Component
public class CustomServiceDiscovery implements McpServiceDiscovery {
    // 实现接口方法
}
```

## 注意事项

1. 确保 Nacos Server 已启动且可访问
2. MCP Server 服务需要先在 Nacos 中注册
3. 需要配置有效的 DashScope API Key 用于 Embedding
4. 默认相似度阈值为 0.2，可根据需要调整
5. 服务更新间隔默认 30 秒

## 相关链接

- [Spring AI Alibaba 文档](https://github.com/alibaba/spring-ai-alibaba)
- [Nacos 官方文档](https://nacos.io/docs/latest/overview/)
- [MCP 协议规范](https://modelcontextprotocol.io/)
