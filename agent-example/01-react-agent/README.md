# ReactAgent.call 执行逻辑

## 1. 调用入口

ReactAgent.call 有多个重载方法（ReactAgent.java:158-179）：

public AssistantMessage call(String message)
public AssistantMessage call(String message, RunnableConfig config)
public AssistantMessage call(UserMessage message)
public AssistantMessage call(List<Message> messages)

所有重载最终都调用 doMessageInvoke（ReactAgent.java:216-235）：

call(message) → doMessageInvoke(message, config) → doInvoke(inputs, config)

## 2. 核心执行流程

┌─────────────────────────────────────────────────────────────────────────┐
│                           ReactAgent.call()                             │
└────────────────────────────────┬────────────────────────────────────────┘
▼
┌─────────────────────────────────────────────────────────────────────────┐
│  1. buildMessageInput() - 构建输入状态 Map                               │
│     • 将消息转换为 List<Message>                                         │
│     • 放入 "messages" 和 "input" key                                     │
└────────────────────────────────┬────────────────────────────────────────┘
▼
┌─────────────────────────────────────────────────────────────────────────┐
│  2. doInvoke() (Agent.java:255-258)                                     │
│     • getAndCompileGraph() - 初始化并编译图                              │
│     • compiledGraph.invoke() - 执行编译后的图                            │
└────────────────────────────────┬────────────────────────────────────────┘
▼
┌─────────────────────────────────────────────────────────────────────────┐
│  3. CompiledGraph.invoke() (CompiledGraph.java:556-558)                 │
│     • 创建 GraphRunner 执行器                                            │
│     • 调用 stream() 方法获取响应流                                       │
│     • 返回最终的 OverAllState                                            │
└────────────────────────────────┬────────────────────────────────────────┘
▼
┌─────────────────────────────────────────────────────────────────────────┐
│  4. 提取 AssistantMessage 作为返回值                                     │
│     • 从 outputKey 或 "messages" 中获取最后一个 AssistantMessage          │
└─────────────────────────────────────────────────────────────────────────┘

## 3. StateGraph 结构（ReAct 循环）

initGraph() 方法（ReactAgent.java:254-343）构建的图结构：
```text
                      START
                        │
                        ▼
          ┌─────────────────────────┐
          │   BEFORE_AGENT Hooks    │  (可选)
          └────────────┬────────────┘
                       ▼
          ┌─────────────────────────┐
          │   BEFORE_MODEL Hooks    │  (可选，包含 InterruptionHook)
          └────────────┬────────────┘
                       ▼
          ┌─────────────────────────┐
          │     AgentLlmNode        │  ← 调用 LLM 模型
          │  (AGENT_MODEL_NAME)     │
          └────────────┬────────────┘
                       ▼
          ┌─────────────────────────┐
          │   AFTER_MODEL Hooks     │  (可选，包含 HumanInTheLoopHook)
          └────────────┬────────────┘
                       ▼
             ┌────────────────────┐
             │   有工具调用吗?     │
             └────────┬───────────┘
            是 │            │ 否
               ▼            ▼
      ┌────────────────┐   ┌────────────────────────┐
      │ AgentToolNode  │   │   AFTER_AGENT Hooks    │
      │(AGENT_TOOL_NAME)│   └───────────┬────────────┘
      └───────┬────────┘               │
              │                        ▼
              │                      END
              │ (继续循环)
              └──────────────────────┐
                                     ▼
                      ┌─────────────────────────┐
                      │   BEFORE_MODEL Hooks    │
                      └─────────────────────────┘
                             (回到模型节点)
```
## 4. 核心节点执行

AgentLlmNode（AgentLlmNode.java:122-275）

1. 消息处理：从 state 获取 messages，处理模板渲染
2. 构建 ModelRequest：包含消息、工具定义、系统提示
3. 拦截器链执行：通过 InterceptorChain.chainModelInterceptors() 构建
4. 调用 ChatClient：执行 LLM 推理
5. 返回结果：AssistantMessage 放入 state

AgentToolNode（AgentToolNode.java:99-181）

1. 解析工具调用：从 AssistantMessage.getToolCalls() 获取
2. 拦截器链执行：通过 InterceptorChain.chainToolInterceptors() 构建
3. 执行工具回调：调用 ToolCallback.call()
4. 构建响应：生成 ToolResponseMessage
5. 更新 state：将工具结果放回 messages

## 5. 边路由逻辑

makeModelToTools（ReactAgent.java:702-751）决定下一步：

// 1. 最后消息是 AssistantMessage 且有 toolCalls → 执行工具
// 2. 最后消息是 AssistantMessage 无 toolCalls → 结束
// 3. 最后消息是 ToolResponseMessage → 检查是否所有工具已执行

makeToolsToModelEdge（ReactAgent.java:753-773）：
- 工具执行完成后，默认返回模型节点继续推理

## 6. 类层次关系

Agent (抽象基类)
└── BaseAgent (基础属性)
└── ReactAgent (ReAct 实现)
├── AgentLlmNode (LLM 调用节点)
└── AgentToolNode (工具执行节点)

## 7. 关键设计特点

1. Hook 系统：支持在不同位置（BEFORE_AGENT、AFTER_AGENT、BEFORE_MODEL、AFTER_MODEL）注入扩展逻辑
2. 拦截器模式：ModelInterceptor 和 ToolInterceptor 提供 AOP 式扩展
3. 响应式执行：基于 Reactor 的 Flux/Mono 实现异步流处理
4. 状态管理：通过 OverAllState 和 KeyStrategy 管理图执行状态