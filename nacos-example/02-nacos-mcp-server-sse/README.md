# Nacos MCP Server 示例

基于 `spring-ai-alibaba-starter-mcp-registry` 实现的 MCP Server 示例，演示如何将 MCP 服务器注册到 Nacos 服务注册中心。

## 功能特性

- 将 MCP Server 注册到 Nacos 服务发现
- 提供时区时间查询工具
- 支持 SSE (Server-Sent Events) 协议
- 支持工具描述的动态同步

## 技术栈

- Spring Boot 3.x
- Spring AI MCP Server (WebFlux)
- Spring AI Alibaba MCP Registry
- Nacos 服务注册中心

## 项目结构

```
02-nacos-mcp-server/
├── src/main/java/com/example/wx/
│   ├── NacosMcpServerApplication.java    # 启动类
│   └── tools/
│       └── TimeTool.java                 # 时间工具
├── src/main/resources/
│   └── application.yaml                  # 配置文件
└── pom.xml
```

## 前置条件

1. JDK 17+
2. Nacos Server 2.x 运行在 `localhost:8848`

## 快速开始

### 1. 启动 Nacos

```bash
# 使用 Docker 启动 Nacos
docker run -d --name nacos \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.3.0
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 验证注册

访问 Nacos 控制台 http://localhost:8848/nacos，在服务列表中查看：
- 服务名：`webflux-mcp-server`
- 分组：`mcp-server`

## 配置说明

```yaml
spring:
  ai:
    mcp:
      server:
        name: nacos-mcp-server          # MCP 服务器名称
        version: 1.0.0                  # 版本号
        type: async                     # 异步模式（WebFlux）
        sse-message-endpoint: /mcp/messages  # SSE 端点路径
        capabilities:
          tool: true                    # 启用工具能力
          resource: true                # 启用资源能力
          prompt: true                  # 启用提示能力
          completion: true              # 启用补全能力
    alibaba:
      mcp:
        nacos:
          server-addr: localhost:8848   # Nacos 服务地址
          namespace: public             # 命名空间
          username: nacos               # 用户名
          password: nacos               # 密码
          register:
            enabled: true               # 启用注册
            service-name: webflux-mcp-server  # 注册的服务名
            service-group: mcp-server   # 服务分组
            service-register: true      # 注册服务实例
            service-ephemeral: true     # 临时实例
```

## 提供的工具

### getCityTimeMethod

获取指定时区的当前时间。

**参数：**
| 名称 | 类型 | 描述 |
|-----|------|------|
| timeZoneId | String | 时区 ID，如 `Asia/Shanghai` |

**返回示例：**
```
The current time zone is Asia/Shanghai and the current time is 2025-12-30 22:30:00 CST
```

## MCP 客户端连接

MCP 客户端可通过以下方式连接：

### 直接连接

```
SSE Endpoint: http://localhost:10018/mcp/messages
```

### 通过 Nacos 服务发现

客户端可从 Nacos 获取服务实例列表，动态发现 MCP Server 地址。

## 相关模块

- `spring-ai-alibaba-starter-mcp-registry` - MCP 注册启动器
- `spring-ai-starter-mcp-server-webflux` - WebFlux MCP Server
