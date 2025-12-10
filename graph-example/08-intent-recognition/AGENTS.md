# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## 项目概述

这是一个基于 Spring AI Alibaba 1.1.0.0 的意图识别示例项目，展示了如何使用 spring-ai-alibaba-graph 工作流引擎构建智能对话系统。

核心技术栈：
- Spring Boot 3.x + Java 17
- Spring AI Alibaba 1.1.0.0
- DashScope (阿里云大模型服务)
- Spring AI Alibaba Graph (工作流引擎)
- Lombok + Hutool

## 常用命令

### 环境准备
项目需要 `.env` 文件配置 API Key：
```
DASH_SCOPE_API_KEY=your_dashscope_api_key
```

### Maven 命令
```bash
# 编译项目（在项目根目录或父目录运行）
mvn clean compile

# 打包项目
mvn clean package

# 运行应用（使用 Spring Boot Maven 插件）
mvn spring-boot:run

# 跳过测试打包
mvn clean package -DskipTests

# 本地安装（如需在其他项目引用）
mvn clean install
```

注意：Maven 本地仓库位置为 `D:\path\apache-maven-3.9.10\mvnrepository`

### 运行应用
```bash
# 方式1：使用 Maven 插件
mvn spring-boot:run

# 方式2：打包后运行 jar
java -jar target/08-intent-recognition-<version>.jar
```

应用启动后监听端口：10028

## 核心架构

### 1. Graph 工作流架构
项目使用 Spring AI Alibaba Graph 实现状态图工作流，核心组件：

- **StateGraph**: 状态图引擎，管理节点执行流程和状态传递
- **NodeAction**: 节点抽象接口，所有节点都实现此接口
- **OverAllState**: 全局状态对象，在节点间传递数据
- **KeyStrategyFactory**: 状态键管理策略工厂，定义不同键的更新策略（Replace/Append）

### 2. 节点设计模式
所有节点都实现 `NodeAction` 接口，通过 `apply(OverAllState state)` 方法：
- 从 state 中读取输入数据
- 执行节点业务逻辑
- 返回 Map<String, Object> 更新全局状态

已实现的节点：
- **LLMNode** (`config/node/LLMNode.java`): 调用大模型节点，支持 ChatClient 和参数模板化
- **RagNode** (`config/node/RagNode.java`): 语义召回节点，使用 DashScopeDocumentRetriever 检索文档

待实现的节点（基于工作流需求）：
- **SlotExtractionNode**: 槽位提取节点，从用户输入中提取意图所需的槽位信息，并判断信息是否完整
- **ToolCallNode**: 工具调用节点，根据意图类型和槽位信息调用对应的业务工具
- **KnowledgeQANode**: 知识问答节点，处理非特定意图的通用问答（可复用 LLMNode）

### 3. 意图识别与槽位提取工作流 (IntentRecognitionGraph.java)

#### 完整工作流架构
```
用户输入 
  ↓
问题重写节点 (LLMNode) 
  ↓
意图识别判断 (条件路由)
  ├─ 意图未完结 → 槽位提取与信息判断节点 (SlotExtractionNode)
  │                    ↓
  │                  槽位补全判断 (条件路由)
  │                    ├─ 信息不全 → 提示用户补充 → 用户输入 (多轮澄清)
  │                    └─ 信息完全 → 工具调用节点 (ToolCallNode) → 意图完结
  │
  ├─ 新意图/意图完结 → 意图识别节点 (LLMNode)
  │                         ↓
  │                     语义召回节点 (RagNode)
  │                         ↓
  │                     意图识别节点 (LLMNode)
  │                         ↓
  │                     返回意图识别结果
  │
  └─ 其他意图 → 知识问答节点 (KnowledgeQANode) → 结果输出
```

#### 核心状态流转

**阶段1: 问题重写**
- 输入: `user_query`, `history_list`
- 节点: `rewriteNode` (LLMNode)
- 输出: `rewrite_query`

**阶段2: 意图状态判断**
- 输入: `intent_status` (新意图/意图切换/意图未完结/意图完结)
- 条件路由决定下一步流程

**阶段3a: 新意图识别流程 (意图完结或新对话)**
1. `rewrite_query` → `ragNode` → `rag_list`
2. `user_query` + `rag_list` → `intentNode` → `intent_result`

**阶段3b: 槽位提取流程 (意图未完结)**
1. `rewrite_query` + `current_intent` → `slotExtractionNode` → `slot_info` + `slot_complete`
2. 判断 `slot_complete`:
   - `false` → 生成 `clarification_message` → 等待用户输入 → 循环
   - `true` → 进入工具调用阶段

**阶段4: 工具调用 (槽位完整)**
- 输入: `current_intent`, `slot_info`
- 节点: `toolCallNode`
- 输出: `tool_result`, `intent_status = "completed"`

**阶段5: 知识问答 (其他意图)**
- 输入: `rewrite_query`
- 节点: `knowledgeQANode`
- 输出: `qa_result`

### 4. 配置和依赖注入
- **DashScopeConfig.java**: 配置 DashScope API 和文档检索器
- **IntentRecognitionGraph.java**: 定义 StateGraph Bean 和节点配置
- **LLMConfig.java**: LLM 节点的配置对象（模型参数、提示词、输入输出键等）

### 5. Prompt 管理
系统提示词使用 Spring Resource 管理，存放在 `src/main/resources/prompts/` 目录：
- `rewrite_prompt.st`: 问题重写提示词模板（支持 StringTemplate 语法）
  - 功能：结合历史对话去除口语化、补全指代，生成完整问题
  - 参数：`{history}` - 历史对话数组（JSON 格式）
  
待创建的 Prompt 文件：
- `intent_prompt.st`: 意图识别提示词模板
  - 功能：识别用户意图类型（如：数据导出、账单查询、聊天记忆、通用问答等）
  - 参数：`{nowDate}`, `{weekDay}`, `{weekOfYear}`, `{rag_list}`
  
- `slot_extraction_prompt.st`: 槽位提取提示词模板
  - 功能：从用户输入中提取意图所需的槽位（如：时间范围、邮箱地址等），判断是否完整
  - 参数：`{current_intent}`, `{history_list}`, `{required_slots}`

加载方式：
```java
@Value("classpath:/prompts/rewrite_prompt.st")
private Resource rewritePrompt;
```

### 6. 环境配置加载
应用启动时通过 `dotenv-java` 加载 `.env` 文件，设置系统属性：
```java
Dotenv dotenv = Dotenv.configure().filename(".env").load();
System.setProperty("DASH_SCOPE_API_KEY", dotenv.get("DASH_SCOPE_API_KEY"));
```

配置参数在 `application.yml` 中引用：
```yaml
spring.ai.dashscope.api-key: ${DASH_SCOPE_API_KEY}
spring.ai.dashscope.index-name: sit-意图语料库
```

## 开发注意事项

### 添加新节点
1. 创建类实现 `NodeAction` 接口
2. 实现 `apply(OverAllState state)` 方法
3. 定义清晰的输入键（从 state 读取）和输出键（返回 Map 中的键）
4. 在 Graph 配置中注册节点

### 修改工作流
1. 在 `IntentRecognitionGraph.java` 中调整节点连接关系
2. 配置 `KeyStrategyFactory` 定义状态键的更新策略：
   - `ReplaceStrategy`: 覆盖原值（如 `user_query`, `rewrite_query`, `intent_result`）
   - `AppendStrategy`: 追加到列表（如 `history_list`）
3. 使用 `StateGraph` API 构建节点连接和条件路由：
   - `.addNode(name, nodeAction)`: 添加节点
   - `.addEdge(from, to)`: 添加无条件边
   - `.addConditionalEdges(from, condition, mapping)`: 添加条件路由

#### 条件路由示例
```java
// 意图状态判断路由
stateGraph.addConditionalEdges("rewriteNode", state -> {
    String intentStatus = state.value("intent_status", "new");
    return intentStatus; // 返回 "new", "incomplete", "completed" 等
}, Map.of(
    "new", "ragNode",           // 新意图 → 语义召回
    "incomplete", "slotNode",   // 意图未完结 → 槽位提取
    "completed", "ragNode"      // 意图完结 → 新一轮意图识别
));

// 槽位完整性判断路由
stateGraph.addConditionalEdges("slotNode", state -> {
    Boolean slotComplete = state.value("slot_complete", false);
    return slotComplete ? "complete" : "incomplete";
}, Map.of(
    "complete", "toolNode",     // 槽位完整 → 工具调用
    "incomplete", END           // 槽位不完整 → 返回提示用户补充
));
```

### 调整 LLM 参数
通过 `LLMConfig.builder()` 设置：
- `model`: 模型名称（如 `DashScopeModel.ChatModel.DEEPSEEK_V3_1.value`）
- `temperature`: 温度参数（0.0-1.0）
- `topP`: 核采样参数
- `systemPrompt`: 系统提示词
- `params`: 动态参数（用于提示词模板变量替换）
- `queryKey`/`outputKey`: 状态中的输入输出键名

### DashScope 文档检索配置
在 `DashScopeConfig.java` 中配置：
- `indexName`: 检索的索引库名称
- `rerankTopN`: 重排序后返回的文档数量

RagNode 默认过滤相似度低于 0.5 的文档。

## 多轮对话与状态管理

### 对话历史存储
对话历史存储在 `history_list` 状态键中，使用 `AppendStrategy` 追加策略：
```java
keyStrategyFactoryBuilder.addPatternStrategy("history_list", new AppendStrategy())
```

历史记录格式（JSON）：
```json
[
  {"role": "user", "content": "用户输入内容", "time": "2024-12-10 10:00:00"},
  {"role": "assistant", "content": "系统回复内容", "time": "2024-12-10 10:00:05"}
]
```

### 意图状态管理
通过 `intent_status` 状态键管理对话流程：
- `new`: 新对话或新意图，进入意图识别流程
- `incomplete`: 当前意图槽位不完整，进入槽位提取流程
- `completed`: 当前意图已完成，下一轮输入视为新意图
- `switched`: 意图切换，进入新意图识别流程

通过 `current_intent` 状态键存储当前识别的意图类型（如 "数据导出"、"账单查询" 等）。

### 槽位管理
通过 `slot_info` 状态键存储已提取的槽位信息（Map 结构）：
```java
{
  "time_range": "2024年12月",
  "email": "user@example.com",
  "data_type": "账单数据"
}
```

通过 `slot_complete` 布尔值标识槽位是否完整。

### 多轮澄清机制
1. SlotExtractionNode 判断槽位不完整时：
   - 设置 `slot_complete = false`
   - 生成 `clarification_message`（如 "请提供时间范围和邮箱地址"）
   - 返回提示信息给用户
   
2. 用户补充信息后：
   - 新输入经过 rewriteNode 结合历史补全
   - 再次进入 SlotExtractionNode 提取补充的槽位
   - 合并到 `slot_info`，重新判断完整性

3. 循环直到 `slot_complete = true`，进入工具调用阶段