# Nacos + MCP 集成全景分析

## 一、5 大核心模块概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        应用层（AI Agent / LLM）                   │
└──────┬──────────────┬──────────────┬───────────────┬────────────┘
       │              │              │               │
   ① Router       ② Gateway     ③ Distributed   ④ Registry
   (智能路由)      (聚合网关)      (分布式客户端)    (服务注册)
       │              │              │               │
       └──────────────┴──────────────┴───────────────┘
                              │
                    ⑤ mcp-common (基础能力)
                              │
                         【Nacos】
```

---

## 二、各模块详细说明

### ① mcp-registry (服务注册)

**功能**：将本地 MCP Server 注册到 Nacos

**依赖**：
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-mcp-registry</artifactId>
</dependency>
```

**配置**：
```yaml
spring:
  ai:
    mcp:
      server:
        name: my-mcp-server
        version: 1.0.0
        type: async          # SYNC 或 ASYNC
        protocol: sse        # stdio、sse、streamable
    alibaba:
      mcp:
        nacos:
          server-addr: localhost:8848
          namespace: public
          username: nacos
          password: nacos
          register:
            enabled: true
            service-name: my-mcp-server
            service-group: mcp-server
            service-register: true
            service-ephemeral: true
```

**核心类**：
- `NacosMcpRegister` - MCP 服务注册器
- `NacosMcpRegisterProperties` - 配置属性
- `CheckCompatibleResult` - 兼容性检查
- `JsonSchemaUtil` - Schema 比较

**工作流程**：
```
1. 启动时从 Nacos 获取服务定义
2. 检查兼容性（版本、协议、工具）
3. 创建或更新服务到 Nacos
4. 订阅服务变更
5. WebServer 初始化后注册实例
```

---

### ② mcp-gateway (聚合网关)

**功能**：聚合多个 MCP Server，提供统一入口

**依赖**：
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-mcp-gateway</artifactId>
</dependency>
```

**配置**：
```yaml
spring:
  ai:
    alibaba:
      mcp:
        gateway:
          enabled: true
          registry: nacos
          sse:
            enabled: true
            endpoint: /sse
          streamable:
            enabled: false
            mcp-endpoint: /mcp
          nacos:
            service-names:
              - database-server
              - file-server
              - weather-server
          oauth:
            enabled: false
            provider:
              client-id: your-client-id
              client-secret: your-secret
              token-uri: https://oauth-server.com/token
              grant-type: client_credentials
            token-cache:
              enabled: true
              max-size: 1000
              refresh-before-expiry: PT5M
```

**核心类**：
- `McpGatewayToolManager` - 工具管理器
- `NacosMcpGatewayToolsProvider` - 工具提供者
- `NacosMcpGatewayToolsInitializer` - 初始化器
- `NacosMcpGatewayToolsWatcher` - 监控器（30秒刷新）
- `McpGatewayOAuthInterceptor` - OAuth 拦截器

**架构图**：
```
┌─────────────────────────────────────────┐
│           MCP Gateway                    │
│  ┌─────────────────────────────────┐    │
│  │   聚合工具列表                    │    │
│  │   - database.query              │    │
│  │   - file.read                   │    │
│  │   - weather.get                 │    │
│  └─────────────────────────────────┘    │
│              ↓ 路由调用                   │
│  ┌────────┐ ┌────────┐ ┌────────┐      │
│  │Database│ │ File   │ │Weather │      │
│  │Server  │ │ Server │ │ Server │      │
│  └────────┘ └────────┘ └────────┘      │
└─────────────────────────────────────────┘
```

---

### ③ mcp-distributed (分布式客户端)

**功能**：从 Nacos 发现并连接 MCP Server

**依赖**：
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-mcp-distributed</artifactId>
</dependency>
```

**配置**：
```yaml
spring:
  ai:
    alibaba:
      mcp:
        nacos:
          server-addr: localhost:8848
          namespace: public
          username: nacos
          password: nacos
          client:
            lazy-init: false
            configs:
              primary-server:
                namespace: public
                server-addr: localhost:8848
            sse:
              connections:
                database-server: "database-server::1.0.0"
                file-server: "file-server::1.0.0"
```

**核心类**：
- `DistributedAsyncMcpClient` - 异步客户端接口
- `SseWebFluxDistributedAsyncMcpClient` - SSE 实现
- `StreamWebFluxDistributedAsyncMcpClient` - Streamable 实现
- `DistributedAsyncMcpToolCallbackProvider` - 工具回调提供者

---

### ④ mcp-router (智能路由)

**功能**：基于语义搜索的智能服务路由

**依赖**：
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-mcp-router</artifactId>
</dependency>
```

**配置**：
```yaml
spring:
  ai:
    alibaba:
      mcp:
        router:
          enabled: true
          service-names:
            - database-server
            - file-server
          vector-store:
            similarity-threshold: 0.2
    dashscope:
      api-key: your-api-key
```

**核心类**：
- `McpRouterService` - 路由核心服务
- `McpProxyService` - MCP 服务代理
- `NacosMcpServiceDiscovery` - Nacos 服务发现
- `SimpleMcpServerVectorStore` - 向量存储

**核心方法**：
| 方法 | 功能 |
|-----|------|
| `searchMcpServer` | 语义搜索匹配的 MCP 服务 |
| `addMcpServer` | 动态添加服务到向量存储 |
| `useTool` | 调用指定服务的工具 |
| `debugMcpService` | 诊断服务连接问题 |

**工作流程**：
```
用户描述："我需要查询数据库"
      ↓
向量化 + 相似度匹配
      ↓
返回：database-server (相似度 0.85)
      ↓
自动连接并调用工具
```

---

### ⑤ mcp-common (基础模块)

**功能**：为其他模块提供 Nacos 集成的基础能力

**核心类**：
- `NacosMcpProperties` - Nacos 连接配置
- `NacosMcpOperationService` - Nacos MCP 操作核心服务
- `NacosMcpSubscriber` - MCP 服务变更订阅接口
- `NacosMcpServerEndpoint` - MCP 服务端点模型

---

## 三、典型架构示例

### 架构 1：微服务 MCP 集群

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ MCP Server A │     │ MCP Server B │     │ MCP Server C │
│ (Registry)   │     │ (Registry)   │     │ (Registry)   │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            ↓
                      ┌──────────┐
                      │  Nacos   │
                      └────┬─────┘
                           ↓
                    ┌─────────────┐
                    │ MCP Gateway │
                    │ (聚合所有工具) │
                    └──────┬──────┘
                           ↓
                    ┌─────────────┐
                    │  AI Agent   │
                    └─────────────┘
```

### 架构 2：智能路由

```
┌─────────────────────────────────────────────┐
│                  AI Agent                    │
│  "帮我查询用户订单信息"                        │
└──────────────────┬──────────────────────────┘
                   ↓
          ┌────────────────┐
          │  MCP Router    │
          │  语义搜索匹配    │
          └───────┬────────┘
                  ↓
    ┌─────────────┼─────────────┐
    ↓             ↓             ↓
┌────────┐  ┌────────┐  ┌────────┐
│ Order  │  │ User   │  │ Product│
│ Server │  │ Server │  │ Server │
└────────┘  └────────┘  └────────┘
```

---

## 四、配置属性总览

| 配置前缀 | 说明 |
|---------|------|
| `spring.ai.alibaba.mcp.nacos` | Nacos 连接配置 |
| `spring.ai.alibaba.mcp.nacos.register` | 服务注册配置 |
| `spring.ai.alibaba.mcp.nacos.client` | 客户端配置 |
| `spring.ai.alibaba.mcp.gateway` | 网关核心配置 |
| `spring.ai.alibaba.mcp.gateway.nacos` | 网关 Nacos 服务配置 |
| `spring.ai.alibaba.mcp.gateway.oauth` | OAuth 认证配置 |
| `spring.ai.alibaba.mcp.router` | 路由配置 |

---

## 五、协议支持

| 协议 | 说明 | 支持 Nacos 注册 |
|-----|------|----------------|
| **stdio** | 标准输入输出 | ❌ 本地 only |
| **SSE** | Server-Sent Events | ✅ |
| **Streamable** | HTTP 流式传输 | ✅ |

---

## 六、快速选择指南

| 场景 | 推荐方案 |
|-----|---------|
| 单个 MCP Server 注册 | `starter-mcp-registry` |
| 多个 MCP Server 聚合 | `starter-mcp-gateway` |
| 客户端连接多个 Server | `starter-mcp-distributed` |
| 智能服务选择 | `starter-mcp-router` |
| 完整微服务架构 | Gateway + Registry + Router |

---

## 七、重要注意事项

1. **兼容性检查**：版本、协议、工具 Schema 必须匹配
2. **工具同步**：从 Nacos 实时同步工具描述和启用状态
3. **临时实例**：`service-ephemeral: true` 时宕机会自动移除
4. **向量缓存**：Router 模块维护本地缓存，版本变更时自动刷新
5. **定时刷新**：Gateway 和 Router 默认 30 秒刷新一次

---

## 八、项目代码路径参考

- 基础配置：`mcp/spring-ai-alibaba-mcp-common/src/main/java/com/alibaba/cloud/ai/mcp/nacos/`
- 注册核心：`mcp/spring-ai-alibaba-mcp-registry/src/main/java/com/alibaba/cloud/ai/mcp/register/`
- 网关核心：`mcp/spring-ai-alibaba-mcp-gateway/src/main/java/com/alibaba/cloud/ai/mcp/gateway/`
- 路由核心：`mcp/spring-ai-alibaba-mcp-router/src/main/java/com/alibaba/cloud/ai/mcp/router/`
