# Spring AI MCP

基于 Spring AI 框架实现的 MCP (Model Context Protocol) 示例项目，演示如何构建 MCP 服务端和客户端进行工具调用。

## 项目简介

本模块是一个完整的 MCP (Model Context Protocol) 实现示例，包含：

- **mcp-server**: MCP 服务端，提供工具定义和执行能力
- **mcp-client**: MCP 客户端，连接服务端并通过 AI 模型调用工具

MCP 是一种标准化的协议，允许 AI 模型与外部工具进行交互。通过本项目，您可以了解如何使用 Spring AI 快速构建基于 MCP 的 AI 应用。

## 主要功能

- MCP 服务端工具注册与暴露
- MCP 客户端工具发现与调用
- 支持 STDIO 和 SSE 两种传输方式
- 集成 DeepSeek 大语言模型进行智能工具调用
- 简单计算器工具示例（支持加减乘除运算）

## 技术栈和依赖

| 依赖名称 | 版本 | 用途 |
|---------|------|------|
| Spring Boot | 3.5.7 | 基础框架 |
| Spring AI | 1.1.0 | AI 能力集成 |
| spring-ai-starter-mcp-server-webflux | 1.1.0 | MCP 服务端支持 |
| spring-ai-starter-mcp-client | 1.1.0 | MCP 客户端支持 |
| spring-ai-starter-model-deepseek | 1.1.0 | DeepSeek 模型集成 |
| dotenv-java | 3.0.0 | 环境变量加载 |
| Java | 17+ | 运行时环境 |

## 项目结构

```
spring-ai-mcp/
├── pom.xml                          # 父模块 POM 配置
├── README.md                        # 项目文档
├── mcp-server/                      # MCP 服务端模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── McpServerApplication.java    # 服务端启动类
│       │   ├── config/
│       │   │   └── McpServerConfig.java     # 服务端配置
│       │   └── tools/
│       │       └── DemoTool.java            # 计算器工具定义
│       └── resources/
│           └── application.yaml             # 服务端配置文件
└── mcp-client/                      # MCP 客户端模块
    ├── pom.xml
    └── src/main/
        ├── java/com/example/wx/
        │   └── McpClientApplication.java    # 客户端启动类
        └── resources/
            ├── application.yaml             # 客户端配置文件
            └── mcp-server.json              # MCP 服务器连接配置
```

### 关键目录说明

| 目录/文件 | 说明 |
|----------|------|
| `mcp-server/` | MCP 服务端实现，负责工具定义和执行 |
| `mcp-client/` | MCP 客户端实现，负责连接服务端并调用工具 |
| `tools/DemoTool.java` | 工具类定义，包含计算器功能 |
| `config/McpServerConfig.java` | 服务端配置，注册工具提供者 |
| `mcp-server.json` | STDIO 模式下的服务器配置 |

## 配置说明

### MCP 服务端配置 (mcp-server/application.yaml)

```yaml
server:
  port: 10014                      # 服务端口

spring:
  application:
    name: spring-ai-mcp-server
  ai:
    mcp:
      server:
        version: 1.0.0             # MCP 版本
        type: sync                 # 同步模式
        instructions: 计算器mcp     # 服务说明
        # stdio: true              # 启用 STDIO 模式（命令行方式）
```

### MCP 客户端配置 (mcp-client/application.yaml)

```yaml
server:
  port: 10015

spring:
  main:
    web-application-type: none     # 非 Web 应用
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY} # DeepSeek API 密钥
      chat:
        options:
          model: deepseek-chat     # 使用的模型
    mcp:
      client:
        stdio:                     # STDIO 传输模式
          servers-configuration: classpath:/mcp-server.json
        # sse:                     # SSE 传输模式（可选）
        #   connections:
        #     server1:
        #       url: http://127.0.0.1:10014
        #       sse-endpoint: /sse
```

### MCP 服务器连接配置 (mcp-server.json)

```json
{
  "mcpServers": {
    "wea": {
      "command": "cmd",
      "args": [
        "/c", "java",
        "-Dspring.main.web-application-type=none",
        "-Dspring.main.banner-mode=off",
        "-Dlogging.pattern.console=",
        "-Dspring.ai.mcp.server.stdio=true",
        "-jar",
        "path/to/mcp-server.jar"
      ],
      "env": {}
    }
  }
}
```

### 环境变量

| 变量名 | 必填 | 说明 |
|-------|------|------|
| `DEEPSEEK_API_KEY` | 是 | DeepSeek API 密钥 |

需要在项目根目录创建 `.env` 文件配置：

```env
DEEPSEEK_API_KEY=your_api_key_here
```

## 使用示例

### 1. 构建 MCP 服务端

```bash
cd spring-ai-mcp/mcp-server
mvn clean package -DskipTests
```

### 2. 运行模式

#### 模式一：STDIO 模式（推荐用于本地测试）

1. 修改 `mcp-client/src/main/resources/mcp-server.json` 中的 jar 包路径
2. 运行客户端：

```bash
cd spring-ai-mcp/mcp-client
mvn spring-boot:run
```

客户端会自动启动服务端 jar 包作为子进程。

#### 模式二：SSE 模式（推荐用于分布式部署）

1. 启动服务端：

```bash
cd spring-ai-mcp/mcp-server
mvn spring-boot:run
```

2. 修改客户端配置启用 SSE 模式
3. 启动客户端

### 3. 示例输出

```
toolCallback.getToolDefinition().name() = calculator
toolCallback.getToolDefinition().description() = 简单的计算器工具

>>> QUESTION: 15+12

>>> ASSISTANT: 这里是计算器工具
15.0 + 12.0 = 27.0
```

## 核心代码说明

### 工具定义 (DemoTool.java)

```java
public class DemoTool {
    @Tool(name = "calculator", description = "简单的计算器工具")
    public String calculator(
            @ToolParam(description = "第一个数字 a") Double a,
            @ToolParam(description = "第二个数字 b") Double b,
            @ToolParam(description = "计算操作符(+ - * /)") String operation
    ) {
        double result = switch (operation) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> throw new IllegalArgumentException("未知操作");
        };
        return String.format("计算结果: %s %s %s = %s", a, operation, b, result);
    }
}
```

### 工具注册配置 (McpServerConfig.java)

```java
@Configuration
public class McpServerConfig {
    @Bean
    public DemoTool demoTool() {
        return new DemoTool();
    }

    @Bean
    public ToolCallbackProvider serverTools(DemoTool demoTool) {
        return ToolCallbackProvider.from(ToolCallbacks.from(demoTool));
    }
}
```

### 客户端调用 (McpClientApplication.java)

```java
@Bean
CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder,
                                    ToolCallbackProvider tools,
                                    ConfigurableApplicationContext context) {
    return args -> {
        ChatClient chatClient = chatClientBuilder.defaultToolCallbacks(tools).build();
        String response = chatClient.prompt("15+12").call().content();
        System.out.println(">>> ASSISTANT: " + response);
    };
}
```

## MCP 传输模式对比

| 特性 | STDIO 模式 | SSE 模式 |
|-----|-----------|---------|
| 部署方式 | 客户端启动子进程 | 独立服务 |
| 适用场景 | 本地开发测试 | 分布式部署 |
| 性能 | 较高 | 网络开销 |
| 配置复杂度 | 简单 | 需配置网络 |

## 扩展开发

### 添加新工具

1. 在 `mcp-server/src/main/java/com/example/wx/tools/` 下创建新工具类
2. 使用 `@Tool` 注解标注工具方法
3. 使用 `@ToolParam` 注解标注参数描述
4. 在 `McpServerConfig` 中注册工具 Bean

### 支持更多 AI 模型

修改 `mcp-client` 的依赖和配置，可替换为其他支持的模型：

- OpenAI
- Anthropic Claude
- 阿里云通义千问
- 其他 Spring AI 支持的模型

## 许可证

本项目遵循 Apache 2.0 许可证。

## 相关链接

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [MCP 协议规范](https://modelcontextprotocol.io/)
- [DeepSeek API 文档](https://platform.deepseek.com/docs)
