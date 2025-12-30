# Spring AI Tool Calling 示例项目

基于 Spring AI 框架的工具调用（Function Calling）示例项目，演示了如何将自定义工具集成到大语言模型中，扩展 AI 的能力。

## 项目简介

本项目展示了 Spring AI 中 Tool Calling 的多种实现方式，允许大语言模型在对话过程中调用外部工具/函数，从而获取实时数据或执行特定操作。通过工具调用，AI 模型可以：

- 获取当前时间、天气等实时信息
- 执行数学计算
- 调用业务逻辑接口
- 与外部系统交互

## 功能特性

- **声明式工具定义**：使用 `@Tool` 注解快速定义工具
- **编程式工具定义**：通过 `MethodToolCallback` 以编程方式构建工具
- **函数式工具**：使用 `FunctionToolCallback` 将函数转换为工具
- **动态工具解析**：通过 `@Bean` 和 `@Description` 动态注册工具
- **手动工具执行**：使用 `ToolCallingManager` 完全控制工具执行流程
- **请求追踪**：集成 `TraceLoggerAdvisor` 实现 AI 调用的链路追踪

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | - | 应用框架 |
| Spring AI | - | AI 集成框架 |
| DeepSeek | - | 大语言模型 |
| Java | 17 | 开发语言 |
| Maven | - | 构建工具 |
| dotenv | - | 环境变量管理 |

## 项目结构

```
spring-ai-tool-calling/
├── src/main/
│   ├── java/com/example/wx/
│   │   ├── ToolCallingApplication.java      # 应用启动类
│   │   ├── advisor/
│   │   │   └── TraceLoggerAdvisor.java      # 请求追踪 Advisor
│   │   ├── compontent/
│   │   │   ├── DateTimeTools.java           # 日期时间工具
│   │   │   └── MathTools.java               # 数学计算工具
│   │   ├── controller/
│   │   │   ├── TestController.java          # 测试控制器
│   │   │   └── ToolController.java          # 工具调用控制器
│   │   └── tools/
│   │       ├── DeclarativeTools.java        # 声明式工具示例
│   │       ├── DynamicTools.java            # 动态工具示例
│   │       ├── FunctionTools.java           # 函数式工具示例
│   │       └── ProgrammaticTools.java       # 编程式工具示例
│   └── resources/
│       └── application.yml                   # 应用配置
├── pom.xml                                    # Maven 依赖配置
└── README.md                                  # 项目文档
```

## 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| spring-boot-starter-web | - | Web 应用基础依赖 |
| spring-ai-starter-model-deepseek | - | DeepSeek 模型集成 |
| java-dotenv | - | .env 环境变量加载 |

## 配置说明

### 环境变量

在项目根目录创建 `.env` 文件，配置以下内容：

```env
DEEPSEEK_API_KEY=your_deepseek_api_key_here
```

### application.yml

```yaml
server:
  port: 10009

spring:
  application:
    name: tool-calling-example
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- DeepSeek API Key

### 2. 编译运行

```bash
# 编译项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

### 3. API 测试

#### 声明式工具调用

```bash
curl "http://localhost:10009/declarative?prompt=现在的时间"
```

#### 编程式工具调用

```bash
curl "http://localhost:10009/programmatic?prompt=现在的时间"
```

#### 函数式工具调用（天气查询）

```bash
curl "http://localhost:10009/weather?prompt=现在广州天气如何，使用摄氏度"
```

#### 动态工具调用

```bash
curl "http://localhost:10009/weather2?prompt=现在广州天气如何，使用摄氏度"
```

#### 手动工具执行

```bash
curl "http://localhost:10009/compute"
```

## 使用示例

### 1. 声明式工具定义

使用 `@Tool` 注解将方法标记为工具：

```java
public class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
```

使用工具：

```java
ChatClient.create(chatModel)
    .prompt("What day is tomorrow?")
    .tools(new DateTimeTools())
    .call()
    .content();
```

### 2. 编程式工具定义

通过 `MethodToolCallback` 构建：

```java
Method method = ReflectionUtils.findMethod(ProgrammaticTools.class, "getTime");
ToolCallback toolCallback = MethodToolCallback.builder()
    .toolDefinition(ToolDefinition.builder()
        .description("Get the current date and time")
        .name("getTime")
        .build())
    .toolMethod(method)
    .toolObject(new ProgrammaticTools())
    .build();
```

### 3. 函数式工具定义

实现 `Function` 接口：

```java
public class WeatherService implements Function<WeatherRequest, WeatherResponse> {
    public WeatherResponse apply(WeatherRequest request) {
        return new WeatherResponse(30.0, Unit.C);
    }
}

ToolCallback toolCallback = FunctionToolCallback.builder("currentWeather", new WeatherService())
    .description("Get the weather in location")
    .inputType(WeatherRequest.class)
    .build();
```

### 4. 动态工具定义

使用 `@Bean` 和 `@Description`：

```java
@Configuration
public class DynamicTools {
    @Bean
    @Description("Get the weather in location")
    Function<WeatherRequest, WeatherResponse> getWeather() {
        return (request) -> new WeatherResponse(37.9, Unit.C);
    }
}
```

### 5. 手动工具执行

完全控制工具执行流程：

```java
ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder().build();

ChatResponse chatResponse = chatModel.call(prompt);

while (chatResponse.hasToolCalls()) {
    ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);
    // 处理结果并发送回模型...
    chatResponse = chatModel.call(newPrompt);
}
```

## 工具调用流程

```
1. 用户请求
   ↓
2. AI 模型分析，决定是否调用工具
   ↓
3. 返回工具调用请求（工具名 + 参数）
   ↓
4. 应用程序执行对应的工具
   ↓
5. 将工具执行结果发送回 AI 模型
   ↓
6. AI 模型基于工具结果生成最终响应
```

## 注解说明

| 注解 | 用途 |
|------|------|
| `@Tool` | 标记方法为可被 AI 调用的工具 |
| `@ToolParam` | 描述工具参数的含义，帮助 AI 理解参数用途 |
| `@Description` | 为 Bean 工具提供描述信息 |

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/declarative` | GET | 声明式工具调用示例 |
| `/programmatic` | GET | 编程式工具调用示例 |
| `/weather` | GET | 函数式工具调用示例 |
| `/weather2` | GET | 动态工具调用示例 |
| `/compute` | GET | 手动工具执行示例 |
| `/test` | GET | 基础测试端点 |

## 许可证

本项目仅供学习和参考使用。
