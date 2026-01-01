# Spring Ai Alibaba
紧跟技术潮流，Spring Ai 学习实践，随心记

项目结构
```text
├─agent-example            // 智能体案例
│   ├─00-flight-booking    // 机票预定
│   ├─01-react-agent       // ReAct 智能体
│   └─playground           // 实验场
│
├─graph-example            // Graph 工作流案例
│   ├─01-chatflow          // 聊天流程
│   ├─02-human-node        // 人工节点
│   ├─03-writing-assistant // 写作助手
│   ├─04-product-analysis  // 产品分析
│   ├─05-observability-langfuse  // Langfuse 可观测性
│   ├─06-slot-extraction   // 槽位提取
│   ├─07-memory-graph      // 记忆图谱
│   └─08-intent-recognition // 意图识别
│
├─nacos-example            // Nacos 集成案例
│   ├─01-nacos-prompt      // Nacos 存储提示词
│   ├─02-nacos-mcp-server  // MCP Server 注册到 Nacos
│   └─03-nacos-mcp-client  // 从 Nacos 发现 MCP Server
│
├─spring-ai-chat           // AI 聊天
│   ├─dashscope-chat       // 通义千问
│   ├─dashscope-chat-memory // 聊天记录保存 内存、Mysql、Postgres
│   ├─deepseek-chat        // DeepSeek
│   └─multiple-chat        // 多模型切换
│
├─spring-ai-evaluation     // AI 评估模块
│
├─spring-ai-mcp            // Spring MCP
│   ├─mcp-client           // MCP Client
│   └─mcp-server           // MCP Server stdio/sse
│
├─spring-ai-observability  // 可观测性最佳实践
│
├─spring-ai-prompt         // AI 提示词
│
├─spring-ai-rag            // RAG 模块
│   ├─rag-bailian-knowledge // 阿里百炼知识库
│   ├─rag-etl              // ETL 文档解析
│   ├─rag-milvus           // Milvus 向量库
│   └─rag-pgvector         // PGVector 向量库
│
├─spring-ai-structured     // 结构化输出
│
├─spring-ai-tool-calling   // 工具调用
│
└─spring-boot-starter-dotenv // .env 文件自动加载模块
```

环境变量配置

```text
DASH_SCOPE_API_KEY=sk-**
DEEPSEEK_API_KEY=sk-***
DEEPSEEK_BASE_URL=https://api.deepseek.com
```
