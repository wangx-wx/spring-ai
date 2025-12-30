# ChatFlow 示例 - 基于 Spring AI Alibaba Graph 的对话流编排

基于 Spring AI Alibaba Graph 框架实现的智能对话流编排示例，演示如何构建带有意图识别和子图调用的多轮对话系统。

## 项目简介

本项目展示了如何使用 Spring AI Alibaba Graph 框架构建一个完整的对话流（ChatFlow）系统。该系统能够：

- 自动识别用户意图（创建待办 / 闲聊）
- 根据不同意图路由到相应的处理节点
- 支持主图调用子图的嵌套流程
- 管理用户的待办事项列表

## 功能特性

- **意图分类**：使用 LLM 自动识别用户输入的意图类型
- **条件路由**：基于意图分类结果动态选择执行路径
- **子图编排**：支持将复杂逻辑封装为子图，实现模块化设计
- **状态管理**：通过 OverAllState 在节点间传递和共享状态
- **LLM 润色**：使用大语言模型对用户输入进行优化处理
- **会话隔离**：支持多用户会话，通过 sessionId 实现状态隔离

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.5.7 | 应用框架 |
| Spring AI | 1.1.0 | AI 集成框架 |
| Spring AI Alibaba Graph | 1.1.0.0-RC2 | 图编排框架 |
| dotenv-java | 3.0.0 | 环境变量管理 |

## 项目结构

```
01-chatflow/
├── pom.xml                                    # Maven 项目配置
└── src/
    └── main/
        ├── java/
        │   └── com/example/wx/
        │       ├── ChatFlowApplication.java   # 应用启动类
        │       ├── conf/
        │       │   ├── TodoChatFlowFactory.java   # 主图工厂类
        │       │   └── TodoSubGraphFactory.java   # 子图工厂类
        │       └── controller/
        │           └── ChatFlowController.java    # REST 控制器
        └── resources/
            └── application.yml                # 应用配置文件
```

## 核心组件说明

### 1. ChatFlowApplication

应用程序入口类，负责：
- 加载 `.env` 文件中的环境变量
- 设置模型服务所需的配置（API Key、Base URL、Model）
- 启动 Spring Boot 应用

### 2. TodoChatFlowFactory（主图工厂）

构建主对话流程图，包含以下节点：

| 节点名称 | 类型 | 职责 |
|---------|------|------|
| `intent` | QuestionClassifierNode | 意图分类，判断用户是否要创建待办 |
| `chat` | LlmNode | 处理普通闲聊请求 |
| `callSubGraph` | 自定义 NodeAction | 调用子图处理待办创建 |
| `mainReply` | AnswerNode | 生成最终回复 |

**流程图：**

```
START -> intent -> [条件判断]
                      |
        +-------------+-------------+
        |                           |
        v                           v
   callSubGraph                   chat
        |                           |
        +-------------+-------------+
                      |
                      v
                  mainReply -> END
```

### 3. TodoSubGraphFactory（子图工厂）

构建待办创建子流程图，包含以下节点：

| 节点名称 | 类型 | 职责 |
|---------|------|------|
| `llm` | LlmNode | 使用 LLM 润色待办内容 |
| `assign` | AssignerNode | 变量赋值，将润色结果赋给输出变量 |
| `answer` | AnswerNode | 生成创建确认消息 |

**流程图：**

```
START -> llm -> assign -> answer -> END
```

### 4. ChatFlowController

REST API 控制器，提供两个接口：

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| POST | `/assistant/chat` | sessionId, userInput | 执行完整对话流程 |
| GET | `/assistant/chat` | userInput | 简单聊天（直接调用 LLM） |

## 状态变量说明

### 主图状态变量

| 变量名 | 类型 | 说明 |
|--------|------|------|
| `session_id` | String | 会话标识符 |
| `user_input` | String | 用户输入内容 |
| `intent_type` | String | 意图分类结果 |
| `chat_reply` | String | 闲聊回复内容 |
| `tasks` | List<String> | 待办事项列表 |
| `created_task` | String | 新创建的待办内容 |
| `answer` | String | 最终回复内容 |

### 子图状态变量

| 变量名 | 类型 | 说明 |
|--------|------|------|
| `task_content` | String | 原始待办内容 |
| `todo_desc` | String | LLM 润色后的待办描述 |
| `created_task` | String | 创建的待办内容（输出） |

## 配置说明

### application.yml

```yaml
server:
  port: 10022                    # 服务端口

spring:
  application:
    name: graph-chatflow-example # 应用名称

  ai:
    openai:
      base-url: ${MODEL_SCOPE_BASE_URL}  # 模型服务地址
      api-key: ${MODEL_SCOPE_API_KEY}    # API 密钥
      chat:
        options:
          model: ${MODEL_SCOPE_MODEL}    # 使用的模型名称
```

### 环境变量配置

在项目根目录创建 `.env` 文件：

```properties
MODEL_SCOPE_BASE_URL=https://your-model-service-url
MODEL_SCOPE_API_KEY=your-api-key
MODEL_SCOPE_MODEL=your-model-name
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- 可用的 OpenAI 兼容 API 服务

### 2. 配置环境变量

在项目根目录创建 `.env` 文件并填入配置信息。

### 3. 编译项目

```bash
cd graph-example/01-chatflow
mvn clean compile
```

### 4. 运行应用

```bash
mvn spring-boot:run
```

### 5. 测试接口

**创建待办：**

```bash
curl -X POST "http://localhost:10022/assistant/chat" \
  -d "sessionId=user-001" \
  -d "userInput=创建待办：明天下午3点开会"
```

**普通聊天：**

```bash
curl -X POST "http://localhost:10022/assistant/chat" \
  -d "sessionId=user-001" \
  -d "userInput=今天天气怎么样"
```

**简单聊天（GET）：**

```bash
curl "http://localhost:10022/assistant/chat?userInput=你好"
```

## 核心依赖

| 依赖 | 说明 |
|------|------|
| spring-ai-starter-model-openai | Spring AI OpenAI 模型集成 |
| spring-ai-autoconfigure-model-chat-client | ChatClient 自动配置 |
| spring-ai-alibaba-graph-core | Spring AI Alibaba 图编排核心库 |
| spring-boot-starter-web | Spring Boot Web 启动器 |
| dotenv-java | 环境变量加载工具 |

## 扩展说明

### 添加新的意图类型

1. 在 `TodoChatFlowFactory` 中修改 `QuestionClassifierNode` 的 `categories` 配置
2. 添加对应的处理节点
3. 更新条件边的路由逻辑

### 自定义子图

参考 `TodoSubGraphFactory` 的实现模式：

1. 定义状态变量和 KeyStrategy
2. 创建 StateGraph 并添加节点
3. 配置节点间的边连接
4. 编译并返回 CompiledGraph

## 相关资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Alibaba 文档](https://java2ai.com/docs/intro/)

## 许可证

本项目仅供学习和演示用途。
