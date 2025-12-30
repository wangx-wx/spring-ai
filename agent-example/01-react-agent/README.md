# React Agent 示例项目

基于 Spring AI Alibaba 的 ReAct（Reasoning + Acting）Agent 智能助手示例项目，展示了如何构建一个具备工具调用能力的智能对话系统。

## 项目简介

本项目是 Spring AI Alibaba 框架中 React Agent 模块的完整示例，演示了：

- 如何创建和配置 React Agent
- 自定义工具（Tool）的实现与集成
- 拦截器（Interceptor）的使用
- 钩子（Hook）机制的应用
- 流式响应处理
- 错误处理与日志记录

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.5.7 | 应用框架 |
| Spring AI Alibaba | 1.1.0.0-RC2 | AI 框架 |
| DashScope SDK | 2.15.1 | 通义千问 API 客户端 |
| dotenv-java | 3.0.0 | 环境变量管理 |

## 项目结构

```
agent-example/01-react-agent/
├── pom.xml                                    # Maven 依赖配置
├── README.md                                  # 项目文档
└── src/main/
    ├── java/com/example/wx/
    │   ├── ReactAgentApplication.java        # 应用启动类
    │   ├── controller/
    │   │   └── ChatController.java           # 对话接口控制器
    │   ├── tools/
    │   │   ├── SearchTool.java               # 搜索工具
    │   │   └── WeatherTool.java              # 天气查询工具
    │   ├── interceptor/
    │   │   ├── DynamicPromptInterceptor.java # 动态提示词拦截器
    │   │   └── ToolErrorInterceptor.java     # 工具错误拦截器
    │   └── hook/
    │       ├── CustomStopConditionHook.java  # 自定义停止条件钩子
    │       ├── LoggingHook.java              # 日志钩子
    │       └── MessageTrimmingHook.java      # 消息修剪钩子
    └── resources/
        └── application.yml                    # 应用配置文件
```

## 核心功能说明

### 1. React Agent

React Agent 是一个基于 ReAct 范式的智能体，能够进行推理（Reasoning）和行动（Acting）：

- **推理**：分析用户输入，确定需要调用哪些工具
- **行动**：调用相应工具获取结果
- **迭代**：基于工具结果继续推理，直到得出最终答案

### 2. 自定义工具（Tools）

| 工具类 | 功能 | 输入参数 |
|--------|------|----------|
| `WeatherTool` | 天气查询 | `city`（城市名称） |
| `SearchTool` | 信息搜索 | `query`（搜索关键词） |

工具实现需实现 `BiFunction<String, ToolContext, String>` 接口。

### 3. 拦截器（Interceptors）

拦截器用于在 Agent 执行过程中插入自定义逻辑：

| 拦截器 | 类型 | 作用 |
|--------|------|------|
| `ToolErrorInterceptor` | ToolInterceptor | 捕获工具调用异常，返回友好错误信息 |
| `DynamicPromptInterceptor` | ModelInterceptor | 根据用户角色动态注入提示词 |

### 4. 钩子（Hooks）

钩子用于在特定节点执行自定义逻辑：

| 钩子 | 类型 | 触发时机 | 作用 |
|------|------|----------|------|
| `LoggingHook` | AgentHook | Agent 执行前后 | 记录 Agent 开始和完成日志 |
| `MessageTrimmingHook` | MessagesModelHook | 每次模型调用前 | 限制消息列表长度，避免超出上下文 |
| `CustomStopConditionHook` | ModelHook | 模型调用前 | 根据自定义条件提前终止执行 |

## 配置说明

### application.yml

```yaml
server:
  port: 10029  # 服务端口

spring:
  application:
    name: react-agent-example
  profiles:
    active: dev
  ai:
    dashscope:
      api-key: ${DASH_SCOPE_API_KEY}  # 通义千问 API 密钥
```

### 环境变量

需要在项目根目录创建 `.env` 文件：

```env
DASH_SCOPE_API_KEY=your_api_key_here
```

获取 API Key：[阿里云百炼平台](https://bailian.console.aliyun.com/)

## 快速开始

### 前置条件

- JDK 17 或更高版本
- Maven 3.6+
- 通义千问 API Key

### 运行步骤

1. **配置 API Key**

在项目根目录（`spring-ai/`）创建 `.env` 文件：

```env
DASH_SCOPE_API_KEY=sk-xxxxxxxxxxxx
```

2. **编译项目**

```bash
mvn clean compile
```

3. **运行应用**

```bash
mvn spring-boot:run -pl agent-example/01-react-agent
```

或在 IDE 中直接运行 `ReactAgentApplication.main()` 方法。

4. **访问接口**

应用启动后，通过浏览器或 curl 访问：

```bash
# 基础查询（使用默认问题）
curl http://localhost:10029/chat/stream

# 自定义查询
curl http://localhost:10029/chat/stream?query=查询上海天气
```

## API 接口

### GET /chat/stream

流式对话接口，返回 Server-Sent Events (SSE) 格式的响应。

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| query | String | 否 | 查询杭州天气并推荐活动 | 用户问题 |

**响应格式：**

```
data: {"node":"service_agent",...}
```

**响应类型：**

- `AGENT_MODEL_STREAMING`: 模型流式输出中
- `AGENT_MODEL_FINISHED`: 模型推理完成
- `AGENT_TOOL_FINISHED`: 工具调用完成
- `AGENT_HOOK_FINISHED`: Hook 执行完成

## 核心依赖

```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring AI Alibaba Agent Framework -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-agent-framework</artifactId>
</dependency>

<!-- Spring AI Alibaba DashScope Starter -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
</dependency>
```

## 扩展开发

### 添加新工具

1. 创建实现 `BiFunction<String, ToolContext, String>` 的类：

```java
public class MyTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
        @JsonProperty(value = "param") @JsonPropertyDescription("参数描述") String param,
        ToolContext toolContext) {
        // 实现工具逻辑
        return "结果";
    }
}
```

2. 在 Agent 构建时注册工具：

```java
ToolCallback myTool = FunctionToolCallback.builder("myTool", new MyTool())
    .description("工具描述")
    .inputType(String.class)
    .build();

ReactAgent agent = ReactAgent.builder()
    .tools(myTool)
    .build();
```

### 添加自定义拦截器

继承相应的拦截器基类并实现 `intercept*` 方法：

```java
public class MyInterceptor extends ModelInterceptor {
    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        // 前置处理
        ModelResponse response = handler.call(request);
        // 后置处理
        return response;
    }

    @Override
    public String getName() {
        return "MyInterceptor";
    }
}
```

### 添加自定义钩子

继承相应的钩子基类：

```java
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class MyHook extends AgentHook {
    @Override
    public String getName() {
        return "myHook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        // 前置逻辑
        return CompletableFuture.completedFuture(Map.of());
    }
}
```

## 相关文档

- [Spring AI Alibaba 官方文档](https://sca.aliyun.com/ai/)
- [通义千问 API 文档](https://help.aliyun.com/zh/dashscope/)
- [ReAct 论文](https://arxiv.org/abs/2210.03629)

## 许可证

本项目仅供学习和参考使用。
