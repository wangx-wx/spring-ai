# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring AI 和阿里云 AI Alibaba Graph Core 框架的示例项目，展示了如何使用图状态机模式构建复杂的 AI 工作流。项目包含 5 个递进式模块，从基础聊天流到高级可观测性集成。

## 构建和运行

### 构建整个项目
```bash
mvn clean install
```

### 构建单个模块
```bash
mvn clean install -pl <module-name>
# 例如: mvn clean install -pl 01-chatflow
```

### 运行模块
```bash
# Windows 环境
java -jar <module-name>\target\<module-name>-${revision}.jar

# 示例
java -jar 01-chatflow\target\01-chatflow-${revision}.jar
```

### 测试单个模块
```bash
mvn test -pl <module-name>
```

## 环境配置

所有模块需要在项目根目录或模块目录创建 `.env` 文件:

```env
MODEL_SCOPE_API_KEY=your_api_key
MODEL_SCOPE_BASE_URL=https://api.openai.com
MODEL_SCOPE_MODEL=gpt-4
DASH_SCOPE_API_KEY=your_dashscope_key
YOUR_BASE64_ENCODED_CREDENTIALS=your_langfuse_credentials
```

## 模块说明

### 01-chatflow (端口: 10022)
基础聊天流程,展示主图-子图架构和条件路由。
- API: `POST /assistant/chat?sessionId=xxx&userInput=yyy`
- 关键类: `TodoChatFlowFactory`, `TodoSubGraphFactory`

### 02-human-node
演示人类在循环中的集成 (Human-in-the-Loop)。
- 关键类: `ExpanderNode`, `HumanFeedbackNode`, `HumanFeedbackDispatcher`

### 03-writing-assistant (端口: 10024)
多阶段文本处理工作流,包含反馈循环和并行处理。
- API: `GET /write?text=xxx`
- 关键类: `WritingAssistantConfig`, `ParallelGraphConfig`

### 04-product-analysis (端口: 10025)
并行处理和自定义状态序列化示例。
- 关键类: `ProductGraphConfig`, `ProductStateSerializer`

### 05-observability-langfuse (端口: 10026)
集成 OpenTelemetry 和 Langfuse 的可观测性示例。
- 关键类: `GraphConfiguration`, `GraphObservabilityApplication`

## 核心架构模式

### StateGraph 构建模式

所有图配置遵循统一模式:

```java
@Bean
public StateGraph xxxGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
    // 1. 定义状态字段的合并策略
    KeyStrategyFactory keyStrategyFactory = KeyStrategyFactoryBuilder.create()
        .addReplaceStrategy("key1")  // 覆盖策略
        .addAppendStrategy("logs")   // 追加策略
        .build();

    // 2. 创建状态图
    StateGraph graph = new StateGraph(keyStrategyFactory);

    // 3. 添加节点 (使用 node_async 包装)
    graph.addNode("nodeName", node_async(new MyNode(chatClient)));

    // 4. 连接边
    graph.addEdge(START, "nodeName");
    graph.addEdge("nodeName", END);

    // 5. 添加条件边 (可选)
    graph.addConditionalEdges("sourceNode",
        edge_async(new MyDispatcher()),
        Map.of("path1", "targetNode1", "path2", "targetNode2")
    );

    return graph;
}
```

### 节点实现模式

```java
public class MyNode implements NodeAction {
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 读取状态
        String input = (String) state.value("inputKey").orElse("");

        // 处理逻辑
        String result = chatClient.prompt(input).call().content();

        // 返回新状态
        return Map.of("outputKey", result);
    }
}
```

### 条件分派器模式

```java
public class MyDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        // 基于状态决定下一个节点
        String condition = (String) state.value("condition").orElse("default");
        return switch (condition) {
            case "positive" -> "positiveNode";
            case "negative" -> "negativeNode";
            default -> END;
        };
    }
}
```

### 并行处理模式

```java
// 两个节点并行执行后在 merge 节点汇合
graph.addEdge(START, "parallel1");
graph.addEdge(START, "parallel2");
graph.addEdge("parallel1", "merge");
graph.addEdge("parallel2", "merge");
```

### 控制器标准模式

```java
@RestController
public class XxxController {
    private final CompiledGraph graph;

    public XxxController(StateGraph stateGraph) throws GraphStateException {
        this.graph = stateGraph.compile();
    }

    @GetMapping("/endpoint")
    public Map<String, Object> process(@RequestParam String input) {
        var config = RunnableConfig.builder()
            .threadId("session-" + UUID.randomUUID())
            .build();
        var result = graph.invoke(Map.of("inputKey", input), config);
        return result.get().data();
    }
}
```

## 关键框架特性

### 预构建节点
框架提供以下预构建节点:
- `LlmNode`: LLM 推理节点
- `AnswerNode`: 答案模板化节点
- `AssignerNode`: 状态赋值/合并节点
- `QuestionClassifierNode`: 问题分类节点

使用工厂方法创建:
```java
LlmNode llmNode = LlmNode.builder(chatClient).build();
```

### 图可视化
启动时自动生成 PlantUML 图:
```java
GraphRepresentation rep = graph.getGraph(GraphRepresentation.Type.PLANTUML, "GraphTitle");
logger.info("\n{}", rep.content());
```

### 状态序列化
处理复杂对象时需要自定义序列化器 (参考 04-product-analysis):
```java
public class CustomStateSerializer extends PlainTextStateSerializer {
    private final ObjectMapper objectMapper;

    @Override
    public String serialize(Object value) {
        // 自定义序列化逻辑
    }

    @Override
    public <T> T deserialize(String value, Class<T> clazz) {
        // 自定义反序列化逻辑
    }
}
```

### 流式处理
使用 `Flux<ChatResponse>` 处理流式输出:
```java
Flux<ChatResponse> flux = chatClient.prompt("...").stream().chatResponse();
Flux<GraphResponse<StreamingOutput>> outputs = FluxConverter.builder()
    .startingNode("nodeName")
    .mapResult(response -> Map.of("key", processedValue))
    .build(flux);
```

## 设计原则遵守

开发时必须严格遵守以下原则:

1. **单一职责**: 每个节点只负责一个具体的处理任务
2. **开闭原则**: 通过 KeyStrategyFactory 扩展状态策略而非修改核心代码
3. **依赖倒置**: 节点依赖 `ChatClient` 等抽象接口,不依赖具体实现
4. **接口隔离**: 使用 `NodeAction` 和 `EdgeAction` 清晰分离节点和边的职责

## 常见开发任务

### 添加新节点
1. 实现 `NodeAction` 接口
2. 在配置类中使用 `node_async()` 包装
3. 通过 `addNode()` 注册到图中
4. 使用 `addEdge()` 或 `addConditionalEdges()` 连接

### 添加新的条件路由
1. 实现 `EdgeAction` 接口
2. 在 `apply()` 方法中返回下一个节点名称
3. 使用 `addConditionalEdges()` 注册

### 添加并行处理
1. 从同一源节点向多个目标节点添加边
2. 创建合并节点处理并行结果
3. 从所有并行节点向合并节点添加边

### 集成可观测性
参考 05-observability-langfuse 模块:
1. 添加 `spring-ai-alibaba-starter-graph-observation` 依赖
2. 配置 OpenTelemetry 导出器
3. 启用观测注解: `management.observations.annotations.enabled: true`
4. 配置采样率: `management.tracing.sampling.probability: 1.0`
