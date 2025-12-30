# Spring AI Chat

基于 Spring AI 框架构建的 AI 聊天示例模块，演示如何使用多种大语言模型（LLM）进行对话交互。

## 项目简介

本模块是 Spring AI 学习项目的聊天功能示例集合，包含四个子模块，分别演示了：

- **dashscope-chat**: 阿里云 DashScope（通义千问）聊天集成
- **dashscope-chat-memory**: 带会话记忆的 DashScope 聊天，支持多种持久化方案
- **deepseek-chat**: DeepSeek 大模型聊天集成
- **multiple-chat**: 多模型切换聊天示例

## 功能特性

- 支持同步/异步（流式）聊天响应
- 支持 ChatModel（底层 API）和 ChatClient（高级 API）两种调用方式
- 支持会话记忆（Chat Memory），可实现多轮对话
- 支持多种记忆持久化方案：内存、MySQL、PostgreSQL
- 支持自定义 LLM 参数（Temperature、TopP、TopK 等）
- 支持 Advisor 拦截器模式，实现日志记录等功能
- 支持多模型动态切换

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 版本 |
| Spring Boot | 3.5.7 | 基础框架 |
| Spring AI | 1.1.0 | AI 集成框架 |
| Spring AI Alibaba | 1.1.0.0-RC2 | 阿里云 AI 扩展 |
| dotenv-java | 3.0.0 | 环境变量管理 |
| MySQL Connector | 8.0.32 | MySQL 数据库驱动 |
| PostgreSQL | - | PostgreSQL 数据库驱动 |

## 项目结构

```
spring-ai-chat/
├── pom.xml                          # 父模块 POM 配置
├── README.md                        # 项目说明文档
│
├── dashscope-chat/                  # DashScope 基础聊天示例
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── DashScopeChatModelApplication.java    # 启动类
│       │   └── controller/
│       │       ├── DashScopeChatClientController.java # ChatClient 控制器
│       │       └── DashScopeChatModelController.java  # ChatModel 控制器
│       └── resources/
│           └── application.yaml     # 应用配置
│
├── dashscope-chat-memory/           # 带记忆的 DashScope 聊天示例
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── ChatMemoryApplication.java           # 启动类
│       │   ├── config/
│       │   │   └── MemoryConfig.java                # 数据源配置
│       │   └── controller/
│       │       ├── InMemoryController.java          # 内存存储控制器
│       │       ├── MysqlMemoryController.java       # MySQL 存储控制器
│       │       └── PgsqlMemoryController.java       # PostgreSQL 存储控制器
│       └── resources/
│           └── application.yaml     # 应用配置
│
├── deepseek-chat/                   # DeepSeek 聊天示例
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── DeepseekChatModelApplication.java    # 启动类
│       │   └── controller/
│       │       ├── ChatClientController.java        # ChatClient 控制器
│       │       └── ChatModelController.java         # ChatModel 控制器
│       └── resources/
│           └── application.yml      # 应用配置
│
└── multiple-chat/                   # 多模型切换示例
    ├── pom.xml
    └── src/main/
        ├── java/com/example/wx/
        │   ├── MultipleChatApplication.java         # 启动类（含命令行交互）
        │   ├── advisor/
        │   │   └── SimpleLoggerAdvisor.java         # 自定义日志 Advisor
        │   └── config/
        │       └── ChatClientConfig.java            # 多模型 ChatClient 配置
        └── resources/
            └── application.yaml     # 应用配置
```

## 快速开始

### 环境准备

1. **JDK 17+**: 确保已安装 Java 17 或更高版本
2. **Maven 3.6+**: 用于项目构建
3. **API Key**: 获取相应的大模型 API Key

### 配置 API Key

在项目根目录创建 `.env` 文件：

```properties
# 阿里云 DashScope API Key（通义千问）
DASH_SCOPE_API_KEY=your_dashscope_api_key

# DeepSeek API Key
DEEPSEEK_API_KEY=your_deepseek_api_key
```

> 获取方式：
> - DashScope: https://dashscope.console.aliyun.com/
> - DeepSeek: https://platform.deepseek.com/

### 数据库配置（仅 dashscope-chat-memory 需要）

如需使用数据库持久化会话记忆，请配置 MySQL 或 PostgreSQL：

**MySQL:**
```sql
CREATE DATABASE chat_memory;
```

**PostgreSQL:**
```sql
CREATE DATABASE db;
```

修改 `dashscope-chat-memory/src/main/resources/application.yaml` 中的数据库连接信息。

### 构建项目

```bash
# 进入项目目录
cd spring-ai-chat

# 编译构建
mvn clean package -DskipTests
```

### 运行示例

**DashScope 基础聊天:**
```bash
cd dashscope-chat
mvn spring-boot:run
# 服务端口: 10000
```

**DashScope 带记忆聊天:**
```bash
cd dashscope-chat-memory
mvn spring-boot:run
# 服务端口: 10020
```

**DeepSeek 聊天:**
```bash
cd deepseek-chat
mvn spring-boot:run
# 服务端口: 10001
```

**多模型切换（命令行交互）:**
```bash
cd multiple-chat
mvn spring-boot:run
# 服务端口: 10013
```

## 核心代码说明

### 1. ChatModel vs ChatClient

Spring AI 提供两种 API 风格：

**ChatModel（底层 API）:**
```java
// 直接使用 ChatModel 进行对话
String response = chatModel.call(new Prompt("你好")).getResult().getOutput().getText();
```

**ChatClient（高级 API，推荐）:**
```java
// 使用 Builder 模式构建，支持链式调用
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(new SimpleLoggerAdvisor())
    .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
    .build();

String response = chatClient.prompt("你好").call().content();
```

### 2. 流式响应

```java
@GetMapping("/stream/chat")
public Flux<String> streamChat(HttpServletResponse response) {
    response.setCharacterEncoding("UTF-8");
    return chatClient.prompt("你好").stream().content();
}
```

### 3. 会话记忆（Chat Memory）

```java
// 配置 MessageChatMemoryAdvisor
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
    .build();

// 调用时指定会话 ID
chatClient.prompt(message)
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
    .call().content();
```

### 4. 自定义 Advisor

```java
public class SimpleLoggerAdvisor implements CallAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("BEFORE: {}", request);
        ChatClientResponse response = chain.nextCall(request);
        logger.info("AFTER: {}", response);
        return response;
    }
}
```

### 5. 多模型配置

```java
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient dashScopeChatClient(DashScopeChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
```

## API 接口说明

### dashscope-chat (端口: 10000)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/client/simple/chat` | GET | ChatClient 同步调用 |
| `/client/stream/chat` | GET | ChatClient 流式调用 |
| `/client/chat/memory/{chatId}?message=xxx` | GET | 带记忆的对话 |
| `/model/simple/chat` | GET | ChatModel 同步调用 |
| `/model/stream/chat` | GET | ChatModel 流式调用 |
| `/model/custom/chat` | GET | 自定义参数调用 |

### dashscope-chat-memory (端口: 10020)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/memory/in/call?query=xxx&conversation_id=xxx` | GET | 内存存储对话 |
| `/memory/in/messages?conversation_id=xxx` | GET | 获取内存中的历史消息 |
| `/memory/mysql/call?query=xxx&conversation_id=xxx` | GET | MySQL 存储对话 |
| `/memory/mysql/messages?conversation_id=xxx` | GET | 获取 MySQL 中的历史消息 |
| `/memory/pgsql/call?query=xxx&conversation_id=xxx` | GET | PostgreSQL 存储对话 |
| `/memory/pgsql/messages?conversation_id=xxx` | GET | 获取 PostgreSQL 中的历史消息 |

### deepseek-chat (端口: 10001)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/model/simple/chat` | GET | ChatModel 简单调用 |
| `/client/ai/customOptions` | GET | ChatClient 自定义参数调用 |

## 配置说明

### application.yaml 核心配置

```yaml
spring:
  ai:
    # DashScope 配置
    dashscope:
      api-key: ${DASH_SCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus-2025-01-25  # 模型名称

    # DeepSeek 配置
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
```

### 可用模型

**DashScope:**
- `qwen-plus-2025-01-25` - 通义千问 Plus
- `deepseek-v3-1` - DeepSeek V3（通过 DashScope 调用）

**DeepSeek:**
- `deepseek-chat` - DeepSeek 对话模型

## 注意事项

1. **API Key 安全**: 请勿将 `.env` 文件提交到版本控制系统，已在 `.gitignore` 中配置忽略

2. **流式响应编码**: 使用流式接口时，需设置响应编码为 UTF-8，否则中文可能乱码
   ```java
   response.setCharacterEncoding("UTF-8");
   ```

3. **会话记忆最大消息数**: 默认配置为 100 条，可根据需要调整 `MAX_MESSAGES` 参数

4. **数据库表自动创建**: 使用 JDBC 存储会话记忆时，相关表会自动创建

5. **多模型切换**: `multiple-chat` 模块需要同时配置 DashScope 和 DeepSeek 的 API Key

6. **端口冲突**: 各子模块使用不同端口，请确保端口未被占用：
   - dashscope-chat: 10000
   - deepseek-chat: 10001
   - multiple-chat: 10013
   - dashscope-chat-memory: 10020

## 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Alibaba 文档](https://java2ai.com/)
- [阿里云 DashScope 文档](https://help.aliyun.com/zh/dashscope/)
- [DeepSeek 开放平台](https://platform.deepseek.com/docs)

## 许可证

本项目仅供学习参考使用。
