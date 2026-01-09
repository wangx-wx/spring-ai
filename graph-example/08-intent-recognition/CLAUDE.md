# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 **Spring AI Alibaba 1.1.0.0** 的意图识别系统，使用 **spring-ai-alibaba-graph** 构建状态图工作流。项目演示了如何结合 LLM 和 RAG（语义检索）构建智能对话系统，支持多轮对话槽位提取和工具调用。

## 构建与运行命令

```bash
# 构建项目
mvn clean compile

# 打包项目
mvn clean package -DskipTests

# 运行应用（需要先配置 .env 文件）
mvn spring-boot:run

# 运行单个测试
mvn test -Dtest=TestClassName#methodName
```

## 环境配置

1. 在项目根目录创建 `.env` 文件：
```
DASH_SCOPE_API_KEY=your_dashscope_api_key
```

2. 应用端口：`10028`

## 目标工作流架构

```
┌─────────────┐
│  用户输入   │◄──────────────────────────────────────┐
└──────┬──────┘                                       │
       ▼                                              │
┌─────────────┐     意图未完结                        │
│  意图状态   │──────────────┐                        │
└──────┬──────┘              │                        │
       │                     ▼                        │
       │ 新意图/       ┌───────────────┐              │
       │ 意图完结      │ 槽位提取 &    │              │
       │              │ 信息判断      │              │
       ▼              └───────┬───────┘              │
┌─────────────┐   意图切换    │                      │
│  意图识别   │◄──────────────┤                      │
└──────┬──────┘              │                      │
       │                     ▼                      │
       │              ┌─────────────┐    信息不全    │
       │              │ 槽位补全？  │───────────────►│
       │              └──────┬──────┘   提示用户补充  │
       │                     │                       │
       │ 其他                │ 槽位完整              │
       ▼                     ▼                       │
┌─────────────┐       ┌─────────────────┐           │
│  知识问答   │       │   工具调用      │           │
└──────┬──────┘       │ ┌─────────────┐ │           │
       │              │ │工具注入     │ │           │
       │              │ │(结合意图)   │ │           │
       │              │ ├─────────────┤ │           │
       │              │ │聊天记忆     │ │           │
       │              │ │......       │ │           │
       │              │ └─────────────┘ │           │
       │              └────────┬────────┘           │
       │                       │                    │
       ▼                       ▼                    │
┌──────────────────────────────────────────────────┐│
│                   意图完结                        │┘
└──────────────────────────────────────────────────┘
```

## 核心工作流节点

### 1. 问题重写节点 (RewriteNode)
- 输入：`user_query`, `history_list`
- 输出：`rewrite_query`
- 职责：结合历史会话，将用户输入重写为完整清晰的问题

### 2. 语义召回节点 (RagNode)
- 输入：`rewrite_query`
- 输出：`rag_list`
- 职责：从意图语料库检索相关文档

### 3. 意图识别节点 (IntentRecognitionNode)
- 输入：`user_query`, `rag_list`
- 输出：`intent_result` (意图类型 + 置信度)
- 职责：识别用户意图，判断是否为新意图或意图切换

### 4. 槽位提取节点 (SlotExtractionNode)
- 输入：`user_query`, `intent_result`, `current_slots`
- 输出：`extracted_slots`, `missing_slots`
- 职责：从用户输入中提取意图所需的槽位信息

### 5. 槽位验证节点 (SlotValidationNode)
- 输入：`extracted_slots`, `intent_schema`
- 输出：`slot_complete` (boolean), `clarification_prompt`
- 职责：验证槽位是否完整，生成澄清提示

### 6. 工具调用节点 (ToolExecutionNode)
- 输入：`intent_result`, `extracted_slots`
- 输出：`tool_result`
- 职责：根据意图和槽位调用相应工具

### 7. 知识问答节点 (KnowledgeQANode)
- 输入：`user_query`, `rag_list`
- 输出：`qa_result`
- 职责：处理非特定意图的通用问答

## 状态键定义

| 状态键 | 类型 | 策略 | 说明 |
|--------|------|------|------|
| `user_query` | String | Replace | 当前用户输入 |
| `history_list` | List | Append | 历史会话记录 |
| `rewrite_query` | String | Replace | 重写后的问题 |
| `rag_list` | List<RagDoc> | Replace | RAG 检索结果 |
| `intent_result` | IntentResult | Replace | 意图识别结果 |
| `intent_state` | String | Replace | 意图状态 (new/continuing/completed) |
| `current_slots` | Map | Replace | 当前已收集的槽位 |
| `missing_slots` | List | Replace | 缺失的必填槽位 |
| `slot_complete` | Boolean | Replace | 槽位是否完整 |
| `clarification_prompt` | String | Replace | 澄清提示语 |
| `tool_result` | Object | Replace | 工具执行结果 |
| `final_response` | String | Replace | 最终响应 |

## 条件路由逻辑

### 意图状态路由
```java
// 从 intent_state_check 节点出发
addConditionalEdges("intent_state_check",
    edge_async(state -> state.value("intent_state", "new")),
    Map.of(
        "new", "intent_recognition",           // 新意图 → 意图识别
        "continuing", "slot_extraction",       // 意图未完结 → 槽位提取
        "completed", "intent_recognition"      // 意图完结 → 重新识别
    ));
```

### 意图类型路由
```java
// 从 intent_recognition 节点出发
addConditionalEdges("intent_recognition",
    edge_async(state -> {
        IntentResult intent = state.value("intent_result");
        return intent.getType(); // specific / qa / other
    }),
    Map.of(
        "specific", "slot_extraction",    // 特定意图 → 槽位提取
        "qa", "knowledge_qa",             // 问答意图 → 知识问答
        "other", "knowledge_qa"           // 其他 → 知识问答
    ));
```

### 槽位完整性路由
```java
// 从 slot_validation 节点出发
addConditionalEdges("slot_validation",
    edge_async(state -> state.value("slot_complete", false) ? "complete" : "incomplete"),
    Map.of(
        "complete", "tool_execution",     // 槽位完整 → 工具调用
        "incomplete", "user_prompt"       // 槽位不全 → 提示用户补充
    ));
```

## 关键组件

| 组件 | 说明 |
|------|------|
| `StateGraph` | 管理节点执行流程和状态转换 |
| `OverAllState` | 节点间传递的全局状态对象（键值对） |
| `NodeAction` | 所有节点实现的抽象接口 |
| `KeyStrategyFactory` | 定义状态更新策略（Replace/Append） |

## 已实现节点

- **LLMNode** (`config/node/LLMNode.java`)：通用 LLM 调用节点，支持 Qwen、Deepseek 等，使用 StringTemplate 语法进行提示词模板化
- **RagNode** (`config/node/RagNode.java`)：语义文档检索节点，使用 `DashScopeDocumentRetriever` 从知识库检索相关文档

## 待实现节点

- **IntentStateNode**：意图状态判断节点
- **SlotExtractionNode**：槽位提取节点
- **SlotValidationNode**：槽位验证节点
- **ToolExecutionNode**：工具调用节点
- **UserPromptNode**：用户提示节点（返回澄清问题）

## 配置类

- **IntentRecognitionGraph** (`config/IntentRecognitionGraph.java`)：定义图工作流和节点配置
- **DashScopeConfig** (`config/DashScopeConfig.java`)：配置 DashScope API 客户端和文档检索器

## 领域模型

- **LLMConfig** (`domain/LLMConfig.java`)：LLM 调用配置（模型、温度、TopP、提示词等），采用 Builder 模式
- **RagDoc** (`domain/RagDoc.java`)：RAG 文档数据模型（Java Record）

### 待实现领域模型

- **IntentResult**：意图识别结果（意图类型、置信度、意图 schema）
- **SlotSchema**：槽位定义（名称、类型、是否必填、验证规则）
- **ConversationContext**：会话上下文（意图状态、已收集槽位、历史记录）

## 提示词管理

提示词模板存放在 `src/main/resources/prompts/` 目录，使用 StringTemplate (`.st`) 格式：
- `rewrite_prompt.st`：问题改写提示词（已实现）
- `intent_prompt.st`：意图识别提示词（待实现）
- `slot_extraction_prompt.st`：槽位提取提示词（待实现）
- `clarification_prompt.st`：槽位澄清提示词（待实现）

模板变量使用 `{variableName}` 语法。

## StateGraph API 参考

### 添加节点
```java
graph.addNode("nodeName", node_async(nodeAction));
```

### 添加普通边
```java
graph.addEdge(START, "firstNode");
graph.addEdge("nodeA", "nodeB");
graph.addEdge("lastNode", END);
```

### 添加条件边
```java
graph.addConditionalEdges("sourceNode",
    edge_async(state -> {
        // 返回路由键
        return state.value("routeKey", "default");
    }),
    Map.of(
        "option1", "targetNode1",
        "option2", "targetNode2",
        "default", END
    ));
```

### 编译执行
```java
CompiledGraph compiled = graph.compile();
OverAllState result = compiled.invoke(Map.of("user_query", "用户输入"));
```

## 工具调用集成

### 定义工具
```java
ToolCallback toolCallback = FunctionToolCallback.builder("toolName", (Request req) -> {
    // 工具逻辑
    return response;
})
.description("工具描述")
.inputType(Request.class)
.build();
```

### 在 ChatClient 中使用
```java
String response = chatClient.prompt(query)
    .tools(toolCallback)
    .call()
    .content();
```

## 依赖说明

- **spring-ai-alibaba-starter-dashscope**：DashScope LLM 服务集成
- **spring-ai-alibaba-graph-core**：StateGraph 工作流引擎
- **dotenv-java**：环境变量加载
- **Hutool Core**：工具库
- **Spring-AI-Alibaba源码地址**：D:\workspace\zz\java\spring-ai-alibaba

## 编码规范

- Java 17
- 遵循 SOLID 设计原则
- 使用 Lombok 简化代码
- 配置与业务逻辑分离
- 提示词外部化管理
- 节点职责单一，通过状态传递数据