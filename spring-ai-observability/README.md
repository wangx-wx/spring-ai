# Spring AI Observability

基于 Spring AI 和阿里云 DashScope 的可观测性示例项目，演示如何在 AI 应用中集成分布式追踪和监控能力。

## 项目简介

本项目展示了如何为 Spring AI 应用添加可观测性支持，包括：

- **分布式追踪**：集成 Micrometer Tracing 和 Zipkin，实现请求链路追踪
- **日志记录**：记录 AI 模型的 Prompt 输入和 Completion 输出
- **健康检查**：通过 Spring Boot Actuator 暴露应用健康状态
- **多模型支持**：演示对话模型、向量嵌入模型、图像生成模型的可观测性配置

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.5.7 | 应用框架 |
| Spring AI | 1.1.0 | AI 能力集成框架 |
| Spring AI Alibaba | 1.1.0.0-RC2 | 阿里云 DashScope 集成 |
| Micrometer Tracing | 1.5.0-M2 | 分布式追踪抽象层 |
| Zipkin Reporter | 3.4.3 | 追踪数据上报 |
| DashScope | - | 阿里云 AI 服务 |

## 项目结构

```
spring-ai-observability/
├── pom.xml                                    # Maven 项目配置
└── src/
    └── main/
        ├── java/
        │   └── com/example/wx/
        │       ├── ObservabilityApplication.java       # 应用启动类
        │       ├── controller/
        │       │   ├── ChatClientController.java       # 对话接口控制器
        │       │   ├── EmbeddingModelController.java   # 向量嵌入控制器
        │       │   ├── ImageModelController.java       # 图像生成控制器
        │       │   └── ToolCallingController.java      # 工具调用控制器
        │       └── tools/
        │           └── TestTools.java                  # 自定义工具类
        └── resources/
            └── application.yml                         # 应用配置文件
```

### 目录说明

| 目录/文件 | 说明 |
|----------|------|
| `controller/` | REST API 控制器，提供各类 AI 能力的 HTTP 接口 |
| `tools/` | 自定义工具类，用于演示 Tool Calling 功能 |
| `application.yml` | 应用配置，包含可观测性相关配置项 |

## 核心功能

### 1. 对话模型可观测性 (ChatClient)

```java
@GetMapping("/stream")
public Flux<String> streamChat(@RequestParam(value = "prompt", defaultValue = "hi") String prompt) {
    return chatClient.prompt(prompt).stream().content();
}
```

支持同步和流式对话，自动记录 Prompt 和 Completion 日志。

### 2. 向量嵌入可观测性 (EmbeddingModel)

```java
@GetMapping
public String embedding() {
    var embeddings = embeddingModel.embed("hello world.");
    return "embedding vector size:" + embeddings.length;
}
```

支持默认配置和自定义模型参数的向量嵌入请求。

### 3. 图像生成可观测性 (ImageModel)

```java
@GetMapping("/generate")
public void image(HttpServletResponse response) {
    ImageResponse imageResponse = imageModel.call(new ImagePrompt(DEFAULT_PROMPT));
    // 返回生成的图像
}
```

### 4. 工具调用可观测性 (Tool Calling)

```java
@GetMapping
public Flux<String> chat(@RequestParam(name = "prompt", defaultValue = "现在的时间") String prompt) {
    return chatClient.prompt(prompt).tools(new TestTools()).stream().content();
}
```

自动追踪工具调用过程，记录工具输入输出。

## 配置说明

### 核心可观测性配置

```yaml
spring:
  ai:
    dashscope:
      observations:
        log-completion: true    # 记录模型输出
        log-prompt: true        # 记录模型输入
    chat:
      client:
        observations:
          log-prompt: true           # 记录 ChatClient Prompt
          log-completion: true       # 记录 ChatClient Completion
          include-error-logging: true # 包含错误日志
    vectorstore:
      observations:
        log-query-response: true     # 记录向量查询响应
    tools:
      observations:
        include-content: true        # 记录工具调用内容
```

### Zipkin 追踪配置

```yaml
management:
  tracing:
    sampling:
      probability: 1.0              # 采样率 100%
  zipkin:
    tracing:
      endpoint: http://192.168.1.11:9411/api/v2/spans
```

### Actuator 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"                # 暴露所有端点
  endpoint:
    health:
      show-details: always          # 显示健康检查详情
```

### 环境变量

| 变量名 | 说明 | 必填 |
|--------|------|------|
| `DASH_SCOPE_API_KEY` | 阿里云 DashScope API Key | 是 |

## 使用方法

### 前置条件

1. **JDK 17+**：确保已安装 Java 17 或更高版本
2. **Zipkin Server**：部署 Zipkin 服务用于收集追踪数据
3. **DashScope API Key**：获取阿里云 DashScope 的 API Key

### 快速开始

1. **配置环境变量**

   在项目根目录创建 `.env` 文件：
   ```
   DASH_SCOPE_API_KEY=your_api_key_here
   ```

2. **修改 Zipkin 地址**

   编辑 `application.yml`，修改 Zipkin 服务地址：
   ```yaml
   management:
     zipkin:
       tracing:
         endpoint: http://your-zipkin-host:9411/api/v2/spans
   ```

3. **启动应用**

   ```bash
   mvn spring-boot:run
   ```

   或者编译后运行：
   ```bash
   mvn clean package
   java -jar target/spring-ai-observability.jar
   ```

### API 接口

应用启动后，默认监听端口 `10013`。

| 接口 | 方法 | 说明 |
|------|------|------|
| `/observability/chat` | GET | 同步对话 |
| `/observability/chat/stream` | GET | 流式对话 |
| `/observability/embedding` | GET | 向量嵌入（默认模型） |
| `/observability/embedding/generic` | GET | 向量嵌入（指定模型） |
| `/observability/image/generate` | GET | 图像生成 |
| `/observability/tools` | GET | 工具调用示例 |

### 接口示例

**对话请求**
```bash
curl "http://localhost:10013/observability/chat?prompt=你好"
```

**流式对话**
```bash
curl "http://localhost:10013/observability/chat/stream?prompt=讲个故事"
```

**向量嵌入**
```bash
curl "http://localhost:10013/observability/embedding"
```

**图像生成**
```bash
curl "http://localhost:10013/observability/image/generate" --output image.png
```

**工具调用**
```bash
curl "http://localhost:10013/observability/tools?prompt=现在几点了"
```

### 查看追踪数据

1. 访问 Zipkin UI：`http://your-zipkin-host:9411`
2. 选择服务名称：`spring-ai-observability-dashscope`
3. 查看请求链路和耗时分析

### Actuator 端点

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/metrics` | 指标数据 |
| `/actuator/info` | 应用信息 |

## 注意事项

1. **API Key 安全**：请勿将 API Key 硬编码到代码或配置文件中，建议使用环境变量或密钥管理服务。

2. **采样率配置**：生产环境建议将 `management.tracing.sampling.probability` 设置为较小的值（如 0.1），避免过多的追踪数据影响性能。

3. **日志敏感信息**：`log-prompt` 和 `log-completion` 会记录用户输入和模型输出，生产环境需注意隐私保护。

4. **Zipkin 服务**：确保 Zipkin 服务可用且网络可达，否则追踪数据将无法上报。

5. **图像模型可观测性**：目前 Spring AI 的图像模型可观测性仅支持 OpenAI，DashScope 的图像模型暂不支持完整的可观测性功能。

6. **HTTP 超时配置**：默认读取超时为 60 秒，可根据实际需求调整：
   ```yaml
   spring:
     http:
       client:
         read-timeout: 60s
   ```

## 依赖说明

### 核心依赖

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter-web` | Web 应用支持 |
| `spring-boot-starter-actuator` | 监控端点支持 |
| `spring-ai-alibaba-starter-dashscope` | DashScope AI 模型集成 |
| `spring-ai-alibaba-starter-tool-calling-weather` | 天气工具调用示例 |
| `micrometer-tracing-bridge-brave` | Micrometer 追踪桥接 |
| `zipkin-reporter-brave` | Zipkin 数据上报 |

## 许可证

本项目作为 Spring AI 学习示例，遵循 Apache License 2.0。
