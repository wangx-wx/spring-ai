# Spring AI Structured Output

Spring AI 结构化输出示例项目，演示如何将 AI 模型的自然语言响应转换为结构化的 Java 对象。

## 项目简介

本项目展示了 Spring AI 框架中 **Structured Output（结构化输出）** 的核心功能。通过使用 `BeanOutputConverter`，可以将大语言模型（LLM）返回的文本响应自动解析并转换为预定义的 Java Bean 对象，从而实现 AI 输出的规范化和可编程处理。

## 功能特性

- **自动类型转换**：使用 `BeanOutputConverter` 将 AI 响应自动转换为 Java 对象
- **格式化提示**：通过预定义的输出格式模板引导 AI 生成符合规范的 JSON 响应
- **JSON 属性排序**：支持 `@JsonPropertyOrder` 注解控制输出字段顺序
- **RESTful API**：提供简洁的 HTTP 接口进行交互测试
- **灵活的模型配置**：支持 OpenAI 兼容的 API 接口（如 DeepSeek）

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.5.7 | 应用框架 |
| Spring AI | 1.1.0 | AI 集成框架 |
| spring-ai-starter-model-openai | - | OpenAI 模型集成 |
| dotenv-java | 3.0.0 | 环境变量管理 |
| Jackson | - | JSON 序列化/反序列化 |

## 项目结构

```
spring-ai-structured/
├── pom.xml                                    # Maven 项目配置
└── src/
    └── main/
        ├── java/
        │   └── com/example/wx/
        │       ├── StructuredApplication.java        # 应用启动类
        │       ├── controller/
        │       │   └── ObjectFormatController.java   # REST 控制器
        │       └── domain/
        │           └── ObjectEntity.java             # 结构化输出实体类
        └── resources/
            └── application.yml                       # 应用配置文件
```

### 核心文件说明

| 文件 | 说明 |
|------|------|
| `StructuredApplication.java` | Spring Boot 启动类，负责加载环境变量 |
| `ObjectFormatController.java` | REST 控制器，提供结构化输出的 API 接口 |
| `ObjectEntity.java` | 定义结构化输出的数据模型（标题、作者、日期、内容） |
| `application.yml` | 应用配置，包括服务端口和 AI 模型配置 |

## 核心依赖

| 依赖 | 用途 |
|------|------|
| `spring-ai-starter-model-openai` | 提供 OpenAI 兼容的 ChatClient 和 ChatModel |
| `spring-boot-starter-web` | 提供 Web 服务支持 |
| `dotenv-java` | 从 .env 文件加载环境变量 |

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 10008                    # 服务端口

spring:
  application:
    name: structured-example     # 应用名称
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY}          # API 密钥（从环境变量读取）
      base-url: https://api.deepseek.com    # API 基础地址
      chat:
        options:
          model: deepseek-chat              # 使用的模型
```

### 环境变量配置

在项目根目录创建 `.env` 文件：

```properties
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_BASE_URL=https://api.deepseek.com
```

## 使用示例

### 结构化输出实体定义

```java
@JsonPropertyOrder({"title", "date", "author", "content"})
public class ObjectEntity {
    @ToolParam(description = "标题")
    private String title;
    private String author;
    private String date;
    private String content;
    // getters and setters...
}
```

### BeanOutputConverter 使用

```java
// 创建转换器
BeanOutputConverter<ObjectEntity> converter = new BeanOutputConverter<>(
    new ParameterizedTypeReference<ObjectEntity>() {}
);

// 获取格式化提示
String format = converter.getFormat();

// 调用 AI 并转换响应
String result = chatClient.prompt(query)
    .user(u -> u.text(promptUserSpec)
        .param("format", format))
    .call().content();

// 转换为 Java 对象
ObjectEntity entity = converter.convert(result);
```

## API 接口

### 1. 基础对话接口

```http
GET /object/chat?query=你的问题
```

**说明**：直接调用 AI 模型，尝试将响应转换为 ObjectEntity 对象。

**示例**：
```bash
curl "http://localhost:10008/object/chat?query=以影子为作者，写一篇200字左右的有关人工智能诗篇"
```

### 2. 格式化对话接口

```http
GET /object/chat-format?query=你的问题
```

**说明**：使用格式化提示词引导 AI 输出纯 JSON 格式，提高转换成功率。

**示例**：
```bash
curl "http://localhost:10008/object/chat-format?query=以影子为作者，写一篇200字左右的有关人工智能诗篇"
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- DeepSeek API Key（或其他 OpenAI 兼容的 API）

### 2. 配置环境变量

在项目根目录（`spring-ai` 目录）创建 `.env` 文件：

```properties
DEEPSEEK_API_KEY=sk-your-api-key
DEEPSEEK_BASE_URL=https://api.deepseek.com
```

### 3. 构建项目

```bash
cd spring-ai-structured
mvn clean package
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

或者直接运行 JAR：

```bash
java -jar target/spring-ai-structured.jar
```

### 5. 测试接口

```bash
# 测试基础对话
curl "http://localhost:10008/object/chat"

# 测试格式化对话
curl "http://localhost:10008/object/chat-format"
```

## 工作原理

1. **BeanOutputConverter** 根据目标 Java Bean 的结构自动生成 JSON Schema 格式的提示词
2. 在调用 AI 模型时，通过 `user()` 方法附加格式化要求
3. AI 模型根据提示词输出符合规范的 JSON 字符串
4. `converter.convert()` 方法将 JSON 字符串反序列化为 Java 对象

## 注意事项

1. **JSON 格式要求**：建议在提示词中明确要求 AI 输出纯 JSON 格式，避免包含 Markdown 代码块标记
2. **错误处理**：转换可能失败，建议添加 try-catch 进行异常处理
3. **模型选择**：不同模型对结构化输出的支持程度不同，建议选择遵循指令能力较强的模型

## 许可证

本项目为学习示例项目，遵循 Apache License 2.0 开源协议。
