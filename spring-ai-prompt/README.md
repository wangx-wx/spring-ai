# Spring AI Prompt

Spring AI Prompt 模板示例项目，演示如何使用 Spring AI 的 Prompt 模板功能来构建动态的 AI 对话系统。

## 项目简介

本模块是 Spring AI 学习项目的子模块之一，专注于展示 **Prompt 模板（Prompt Template）** 的使用方法。通过 StringTemplate（.st 文件）定义系统提示词模板，并在运行时动态填充变量，实现灵活的 AI 角色扮演对话功能。

### 核心功能

- **动态 System Prompt**：使用模板文件定义系统提示词，支持变量占位符
- **角色扮演对话**：通过参数化配置 AI 的名称和说话风格
- **流式响应**：采用响应式编程模型，支持流式输出 AI 回复

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.7 | 应用框架 |
| Spring AI | 1.1.0 | AI 集成框架 |
| OpenAI Starter | - | 兼容 OpenAI API 的模型接入 |
| dotenv-java | 3.0.0 | 环境变量管理 |

## 项目结构

```
spring-ai-prompt/
├── pom.xml                                    # Maven 项目配置
└── src/
    └── main/
        ├── java/
        │   └── com/example/wx/
        │       ├── PromptApplication.java     # 应用启动类
        │       └── controller/
        │           └── RoleController.java    # 角色扮演控制器
        └── resources/
            ├── application.yml                # 应用配置文件
            └── prompts/
                └── system-message.st          # System Prompt 模板
```

### 目录说明

| 目录/文件 | 说明 |
|-----------|------|
| `controller/` | REST API 控制器，处理 HTTP 请求 |
| `prompts/` | Prompt 模板文件目录，存放 .st 模板文件 |
| `application.yml` | Spring Boot 配置文件 |

## 核心依赖

| 依赖 | 用途 |
|------|------|
| `spring-ai-starter-model-openai` | 提供 OpenAI 兼容模型的集成能力 |
| `spring-boot-starter-web` | Web 应用支持，提供 REST API 能力 |
| `dotenv-java` | 从 .env 文件加载环境变量 |

## 核心代码实现

### 1. Prompt 模板文件

项目使用 StringTemplate（.st 格式）定义系统提示词模板：

```st
You are a helpful AI assistant.
You are an AI assistant that helps people find information.
Your name is {name}
You should reply to the user's request with your name and also in the style of a {voice}.
```

**模板变量说明**：
- `{name}` - AI 助手的名称
- `{voice}` - AI 的说话风格（如 pirate 海盗风格）

### 2. 控制器实现

`RoleController` 展示了 Prompt 模板的核心用法：

```java
@RestController
@RequestMapping("prompt")
public class RoleController {
    private final ChatClient chatClient;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;  // 注入模板资源

    public RoleController(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("中文回答").build();
    }

    @GetMapping("/roles")
    public Flux<String> generate(...) {
        // 1. 创建用户消息
        UserMessage userMessage = new UserMessage(message);

        // 2. 使用 SystemPromptTemplate 加载模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);

        // 3. 填充模板变量
        Message systemMessage = systemPromptTemplate.createMessage(
            Map.of("name", name, "voice", voice)
        );

        // 4. 构建 Prompt 并调用模型
        return chatClient.prompt(new Prompt(List.of(userMessage, systemMessage)))
                .stream().content();
    }
}
```

### 设计模式

- **模板方法模式**：通过 `SystemPromptTemplate` 将提示词模板化，分离模板定义与变量填充
- **依赖注入**：使用 Spring 的 `@Value` 注入资源文件，`ChatClient.Builder` 构建器模式创建客户端
- **响应式编程**：使用 `Flux<String>` 实现流式响应

## 配置说明

### application.yml

```yaml
server:
  port: 10007                          # 服务端口

spring:
  application:
    name: prompt-example               # 应用名称
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY}     # API 密钥（从环境变量读取）
      base-url: https://api.deepseek.com  # API 基础地址
      chat:
        options:
          model: deepseek-chat         # 使用的模型
```

### 环境变量

项目需要在根目录创建 `.env` 文件，配置以下环境变量：

```env
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_BASE_URL=https://api.deepseek.com
```

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- DeepSeek API 密钥（或其他 OpenAI 兼容的 API）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd spring-ai/spring-ai-prompt
   ```

2. **配置环境变量**

   在项目根目录（spring-ai/）创建 `.env` 文件：
   ```env
   DEEPSEEK_API_KEY=your_api_key
   DEEPSEEK_BASE_URL=https://api.deepseek.com
   ```

3. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

4. **运行应用**
   ```bash
   java -jar target/spring-ai-prompt.jar
   ```

   或使用 Maven：
   ```bash
   mvn spring-boot:run
   ```

### API 接口

#### 角色扮演对话

**请求**
```http
GET http://localhost:10007/prompt/roles
```

**参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| message | String | 否 | (关于海盗的问题) | 用户输入的问题 |
| name | String | 否 | Bob | AI 助手的名称 |
| voice | String | 否 | pirate | AI 的说话风格 |

**示例**
```bash
# 默认海盗风格
curl "http://localhost:10007/prompt/roles"

# 自定义风格
curl "http://localhost:10007/prompt/roles?name=Alice&voice=poet&message=请给我讲一个故事"
```

**响应**

流式返回 AI 生成的文本内容。

## 开发指南

### 添加新的 Prompt 模板

1. 在 `src/main/resources/prompts/` 目录下创建新的 `.st` 文件
2. 使用 `{variableName}` 语法定义变量占位符
3. 在控制器中通过 `@Value` 注入并使用 `SystemPromptTemplate` 解析

### 切换模型

修改 `application.yml` 中的配置即可切换到其他 OpenAI 兼容的模型：

```yaml
spring:
  ai:
    openai:
      base-url: https://api.openai.com  # OpenAI 官方
      chat:
        options:
          model: gpt-4o
```

## 许可证

本项目为学习示例项目，仅供参考学习使用。
